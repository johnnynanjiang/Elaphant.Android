package com.breadwallet.presenter.activities.crc

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import com.breadwallet.R
import com.breadwallet.did.DidDataSource
import com.breadwallet.presenter.customviews.LoadingDialog
import com.breadwallet.presenter.entities.VoteEntity
import com.breadwallet.presenter.interfaces.BRAuthCompletion
import com.breadwallet.tools.adapter.VoteNodeAdapter
import com.breadwallet.tools.animation.UiUtils
import com.breadwallet.tools.manager.BRSharedPrefs
import com.breadwallet.tools.security.AuthManager
import com.breadwallet.tools.threads.executor.BRExecutor
import com.breadwallet.tools.util.BRConstants
import com.breadwallet.tools.util.StringUtil
import com.breadwallet.tools.util.Utils
import com.breadwallet.vote.CityEntity
import com.breadwallet.vote.CrcRankEntity
import com.breadwallet.vote.PayLoadEntity
import com.breadwallet.wallet.wallets.ela.ElaDataSource
import com.breadwallet.wallet.wallets.ela.WalletElaManager
import com.breadwallet.wallet.wallets.ela.response.create.ElaOutput
import com.elastos.jni.UriFactory
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.math.BigDecimal
import java.util.*


@Suppress("UNREACHABLE_CODE")
class CrcVoteActivity : AppCompatActivity() {

    private var mLoadingDialog: LoadingDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crc_vote_layout)

        mLoadingDialog = LoadingDialog(this, R.style.progressDialog)
        initView()
        initLinster()
        initData()
    }

    private val uriFactory: UriFactory = UriFactory()
    fun initView() {
        if (intent!=null) {
            if (!StringUtil.isNullOrEmpty(intent.action) && intent.action==Intent.ACTION_VIEW) {
                uriFactory.parse(intent.data.toString())
            } else {
                uriFactory.parse(intent.getStringExtra("crc_scheme_uri"))
            }
        }
    }

    fun initLinster() {
        findViewById<View>(R.id.back_button).setOnClickListener {
            finish()
        }

        findViewById<View>(R.id.vote_cancle_btn).setOnClickListener {
            finish()
        }

        findViewById<View>(R.id.view_all_members).setOnClickListener {
            UiUtils.startCrcMembersActivity(this, uriFactory.url)
        }

        findViewById<View>(R.id.vote_confirm_btn).setOnClickListener {
            AuthManager.getInstance().authPrompt(this, this.getString(R.string.pin_author_vote), getString(R.string.pin_author_vote_msg), true, false, object : BRAuthCompletion {
                override fun onComplete() {
                    showDialog()
                    BRExecutor.getInstance().forLightWeightBackgroundTasks().execute(Runnable {
                        //dpos payload
                        val dposNodes = Utils.spliteByComma(BRSharedPrefs.getCandidate(this@CrcVoteActivity))
                        val address = WalletElaManager.getInstance(this@CrcVoteActivity).address
                        val amout = 0L
                        var publickeys: ArrayList<PayLoadEntity>?
                        if(dposNodes == null) {
                            publickeys = null
                        } else {
                            publickeys = ArrayList()
                            for(dposNode in dposNodes) {
                                val payLoadEntity = PayLoadEntity()
                                payLoadEntity.candidate = dposNode
                                payLoadEntity.value = amout
                                publickeys.add(payLoadEntity)
                            }
                        }

                        //crc payload
                        val crcNodes = Utils.spliteByComma(uriFactory.candidates)
                        var crcCandidates: ArrayList<PayLoadEntity>?
                        if(crcNodes == null) {
                            crcCandidates = null
                        } else {
                            crcCandidates = ArrayList()
                            for(i in crcNodes.indices) {
                                val payLoadEntity = PayLoadEntity()
                                payLoadEntity.candidate = crcNodes[i]
                                payLoadEntity.value = amout
                                crcCandidates.add(payLoadEntity)
                            }
                        }

                        val transactions = ElaDataSource.getInstance(this@CrcVoteActivity).
                                createElaTx(address, address, amout, "vote", publickeys, crcCandidates) { elaOutput: ElaOutput, payLoadEntities: MutableList<PayLoadEntity> ->
                                   try {
                                       val crcAmounts = Utils.spliteByComma(uriFactory.votes)?: return@createElaTx
                                       for(i in crcAmounts.indices) {
                                           payLoadEntities[i].value = BigDecimal(crcAmounts[i]).multiply(BigDecimal(elaOutput.amount)).divide(BigDecimal(100)).toLong()
                                       }
                                       Log.d("", "")
                                   } catch (e: Exception) {
                                       e.printStackTrace()
                                   }
                                }
                        if (null == transactions) {
                            dismissDialog()
                            finish()
                            return@Runnable
                        }

                        val mRwTxid = ElaDataSource.getInstance(this@CrcVoteActivity).sendElaRawTx(transactions)
                        if (StringUtil.isNullOrEmpty(mRwTxid)) {
                            dismissDialog()
                            finish()
                            return@Runnable
                        }
                        callBackUrl(mRwTxid)
                        callReturnUrl(mRwTxid)
                        BRSharedPrefs.cacheCrcVotes(this@CrcVoteActivity, uriFactory.votes)
                        dismissDialog()
                        finish()
                    })
                }

                override fun onCancel() {
                    //nothing
                }
            })
        }
    }

    fun initData() {
        val dposNodesTv = findViewById<TextView>(R.id.dpos_vote_nodes_tv)
        val crcNodesTv = findViewById<TextView>(R.id.crc_vote_nodes_tv)
        val balance = BRSharedPrefs.getCachedBalance(this, "ELA")

        //total vote counts
        findViewById<TextView>(R.id.votes_counts).text = balance.subtract(BigDecimal(0.0001)).toLong().toString()

//        if(StringUtil.isNullOrEmpty(BRSharedPrefs.getCandidate(this))) "" else BRSharedPrefs.getCandidate(this).trim()
        val dposNodes = Utils.spliteByComma(BRSharedPrefs.getCandidate(this))
        val crcNodes = Utils.spliteByComma(uriFactory.candidates)

        // dpos vote counts
        if(null==dposNodes || dposNodes.count() <= 0 ) {
            dposNodesTv.visibility = View.GONE
            findViewById<View>(R.id.council_title).visibility = View.GONE
            findViewById<View>(R.id.vote_paste_tv).visibility = View.GONE
            findViewById<View>(R.id.council_lv).visibility = View.GONE
        } else {
            dposNodesTv.text = String.format(getString(R.string.crc_vote_dpos_nodes), dposNodes.count())
            val producers = ElaDataSource.getInstance(this).getProducersByPK(dposNodes)
            findViewById<ListView>(R.id.council_lv).adapter = VoteNodeAdapter(this, producers)
        }
        // crc counts
        crcNodesTv.text = String.format(getString(R.string.crc_vote_crc_nodes), crcNodes.count())

        //balance
        findViewById<TextView>(R.id.vote_ela_balance).text = String.format(getString(R.string.vote_balance), balance.toString())
        // fee
        BRExecutor.getInstance().forLightWeightBackgroundTasks().execute {
            try {
                val fee = ElaDataSource.getInstance(this).nodeFee
                val feeStr = BigDecimal(fee).divide(BigDecimal(100000000), 8, BRConstants.ROUNDING_MODE).toString()
                BRExecutor.getInstance().forMainThreadTasks().execute {
                    findViewById<TextView>(R.id.vote_text_hint1).setText(String.format(getString(R.string.vote_hint), feeStr)) }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        //crc members lv
        BRExecutor.getInstance().forLightWeightBackgroundTasks().execute {
            CrcDataSource.getInstance(this).getCrcWithRank()

            val cityStr = readCities("city/cities") ?: return@execute
            val cities = Gson().fromJson<List<CityEntity>>(cityStr, object : TypeToken<List<CityEntity>>() {
            }.type)
            CrcDataSource.getInstance(this).updateMessage(cities)

            BRExecutor.getInstance().forMainThreadTasks().execute{
                findViewById<FlowLayout>(R.id.numbers_float_layout).also {
                    with(it) {
                        setAdapter(
                                CrcDataSource.getInstance(this@CrcVoteActivity).getMembersByIds(crcNodes),
                                R.layout.crc_member_layout,
                                object : FlowLayout.ItemView<CrcRankEntity>() {
                                    override fun getCover(item: CrcRankEntity?, holder: FlowLayout.ViewHolder?, inflate: View?, position: Int) {
                                        holder?.setText(R.id.tv_label_name, item?.Nickname + "|" + item?.Area)
                                    }
                                }
                        )
                    }
                }
            }
        }

    }

    fun readCities(filename : String): String? {
        try {
            val inputStream = assets.open(filename)
            val size = inputStream.available()
            var buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()

            return String(buffer)
        } catch (e : Exception) {
            e.printStackTrace()
        }

        return null
    }

    private fun dismissDialog() {
        runOnUiThread {
            if (!isFinishing)
                mLoadingDialog?.dismiss()
        }
    }


    private fun showDialog() {
        runOnUiThread {
            if (!isFinishing)
                mLoadingDialog?.show()
        }
    }

    private fun callReturnUrl(txId: String) {
        if (StringUtil.isNullOrEmpty(txId)) return
        val returnUrl = uriFactory.returnUrl
        if (StringUtil.isNullOrEmpty(returnUrl)) {
            Toast.makeText(this@CrcVoteActivity, "returnurl is empty", Toast.LENGTH_SHORT).show()
            return
        }
        val url: String
        if (returnUrl.contains("?")) {
            url = "$returnUrl&TXID=$txId"
        } else {
            url = "$returnUrl?TXID=$txId"
        }
        DidDataSource.getInstance(this@CrcVoteActivity).callReturnUrl(url)
    }

    private fun callBackUrl(txid: String) {
        try {
            if (StringUtil.isNullOrEmpty(txid)) return
            val backurl = uriFactory.callbackUrl
            if (StringUtil.isNullOrEmpty(backurl)) return
            val txEntity = VoteEntity()
            txEntity.TXID = txid
            val ret = DidDataSource.getInstance(this).urlPost(backurl, Gson().toJson(txEntity))
        } catch (e: Exception) {
            Toast.makeText(this@CrcVoteActivity, "callback error", Toast.LENGTH_SHORT)
            e.printStackTrace()
        }

    }

}