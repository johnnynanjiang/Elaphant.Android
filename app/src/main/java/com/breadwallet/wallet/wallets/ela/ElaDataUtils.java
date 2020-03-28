package com.breadwallet.wallet.wallets.ela;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.breadwallet.tools.manager.BRSharedPrefs;
import com.breadwallet.tools.sqlite.BRSQLiteHelper;
import com.breadwallet.tools.util.StringUtil;
import com.breadwallet.vote.ProducerEntity;
import com.breadwallet.wallet.wallets.ela.data.HistoryTransactionEntity;
import com.breadwallet.wallet.wallets.ela.data.TxProducerEntity;
import com.breadwallet.wallet.wallets.ela.response.create.ElaOutput;
import com.breadwallet.wallet.wallets.ela.response.create.ElaTransaction;
import com.breadwallet.wallet.wallets.ela.response.create.ElaUTXOInput;
import com.breadwallet.wallet.wallets.ela.response.history.History;
import com.elastos.jni.Utility;
import com.elastos.jni.utils.HexUtils;

import org.elastos.sdk.keypair.ElastosKeypair;

import java.math.BigDecimal;
import java.util.List;

public class ElaDataUtils {

    public static final String ELA_NODE_KEY = "elaNodeKey";
    public static final String ELA_NODE =  "node1.elaphant.app" /*"dev.elapp.org"*/;

    public static String getUrlByVersion(Context context, String api, String version) {
        String node = BRSharedPrefs.getElaNode(context, ELA_NODE_KEY);
        if(StringUtil.isNullOrEmpty(node)) node = ELA_NODE;
        return new StringBuilder("https://").append(node).append("/api/").append(version).append("/").append(api).toString();
    }

    public static String getUrl(Context context, String api){
        String node = BRSharedPrefs.getElaNode(context, ELA_NODE_KEY);
        if(StringUtil.isNullOrEmpty(node)) node = ELA_NODE;
        return new StringBuilder("https://").append(node).append("/api/1/").append(api).toString();
    }

    public static String getMeno(String value){
        if(value==null || !value.contains("msg") || !value.contains("type") || !value.contains(",")) return "";
        if(value.contains("msg:")){
            String[] msg = value.split("msg:");
            if(msg!=null && msg.length==2){
                return msg[1];
            }
        }
        return "";
    }


    public static ContentValues createHistoryValues(HistoryTransactionEntity entity) {
        ContentValues value = new ContentValues();
        value.put(BRSQLiteHelper.ELA_COLUMN_ISRECEIVED, entity.isReceived? 1:0);
        value.put(BRSQLiteHelper.ELA_COLUMN_TIMESTAMP, entity.status.equalsIgnoreCase("pending")?System.currentTimeMillis()/1000:entity.timeStamp);
        value.put(BRSQLiteHelper.ELA_COLUMN_BLOCKHEIGHT, entity.blockHeight);
        value.put(BRSQLiteHelper.ELA_COLUMN_HASH, entity.hash);
        value.put(BRSQLiteHelper.ELA_COLUMN_TXREVERSED, entity.txReversed);
        value.put(BRSQLiteHelper.ELA_COLUMN_FEE, entity.fee);
        value.put(BRSQLiteHelper.ELA_COLUMN_TO, entity.toAddress);
        value.put(BRSQLiteHelper.ELA_COLUMN_FROM, entity.fromAddress);
        value.put(BRSQLiteHelper.ELA_COLUMN_BALANCEAFTERTX, entity.balanceAfterTx);
        value.put(BRSQLiteHelper.ELA_COLUMN_TXSIZE, entity.txSize);
        value.put(BRSQLiteHelper.ELA_COLUMN_AMOUNT, entity.amount);
        value.put(BRSQLiteHelper.ELA_COLUMN_MENO, entity.memo);
        value.put(BRSQLiteHelper.ELA_COLUMN_ISVALID, entity.isValid?1:0);
        value.put(BRSQLiteHelper.ELA_COLUMN_ISVOTE, entity.isVote?1:0);
        value.put(BRSQLiteHelper.ELA_COLUMN_PAGENUMBER, entity.pageNumber);
        value.put(BRSQLiteHelper.ELA_COLUMN_STATUS, entity.status);
        value.put(BRSQLiteHelper.ELA_COLUMN_TPYE, entity.type);
        value.put(BRSQLiteHelper.ELA_COLUMN_TXTPYE, entity.txType);

        return value;
    }

    public static ContentValues createProducerValues(ProducerEntity entity) {
        ContentValues value = new ContentValues();
        value.put(BRSQLiteHelper.PEODUCER_PUBLIC_KEY, entity.Producer_public_key);
        value.put(BRSQLiteHelper.PEODUCER_VALUE, entity.Value);
        value.put(BRSQLiteHelper.PEODUCER_RANK, entity.Rank);
        value.put(BRSQLiteHelper.PEODUCER_ADDRESS, entity.Address);
        value.put(BRSQLiteHelper.PEODUCER_NICKNAME, entity.Nickname);
        value.put(BRSQLiteHelper.PEODUCER_VOTES, entity.Votes);

        return value;
    }

    public static final String[] elaHistoryColumn = {
            BRSQLiteHelper.ELA_COLUMN_ISRECEIVED,
            BRSQLiteHelper.ELA_COLUMN_TIMESTAMP,
            BRSQLiteHelper.ELA_COLUMN_BLOCKHEIGHT,
            BRSQLiteHelper.ELA_COLUMN_HASH,
            BRSQLiteHelper.ELA_COLUMN_TXREVERSED,
            BRSQLiteHelper.ELA_COLUMN_FEE,
            BRSQLiteHelper.ELA_COLUMN_TO,
            BRSQLiteHelper.ELA_COLUMN_FROM,
            BRSQLiteHelper.ELA_COLUMN_BALANCEAFTERTX,
            BRSQLiteHelper.ELA_COLUMN_TXSIZE,
            BRSQLiteHelper.ELA_COLUMN_AMOUNT,
            BRSQLiteHelper.ELA_COLUMN_MENO,
            BRSQLiteHelper.ELA_COLUMN_ISVALID,
            BRSQLiteHelper.ELA_COLUMN_ISVOTE,
            BRSQLiteHelper.ELA_COLUMN_PAGENUMBER,
            BRSQLiteHelper.ELA_COLUMN_STATUS,
            BRSQLiteHelper.ELA_COLUMN_TPYE,
            BRSQLiteHelper.ELA_COLUMN_TXTPYE
    };

    public static HistoryTransactionEntity cursorToTxEntity(Cursor cursor) {
        return new HistoryTransactionEntity(cursor.getInt(0)==1,
                cursor.getLong(1),
                cursor.getInt(2),
                cursor.getBlob(3),
                cursor.getString(4),
                cursor.getLong(5),
                cursor.getString(6),
                cursor.getString(7),
                cursor.getLong(8),
                cursor.getInt(9),
                cursor.getLong(10),
                cursor.getString(11),
                cursor.getInt(12)==1,
                cursor.getInt(13)==1,
                cursor.getInt(14),
                cursor.getString(15),
                cursor.getString(16),
                cursor.getString(17));
    }

    public static TxProducerEntity cursorToTxProducerEntity(Cursor cursor){
        return new TxProducerEntity(cursor.getString(1),
                cursor.getString(2),
                cursor.getString(3));
    }

    public static ProducerEntity cursorToProducerEntity(Cursor cursor) {
        return new ProducerEntity(cursor.getString(0),
                cursor.getString(1),
                cursor.getInt(2),
                cursor.getString(3),
                cursor.getString(4),
                cursor.getString(5));
    }

    public static HistoryTransactionEntity setHistoryEntity(History history, int pageNumber) {
        HistoryTransactionEntity historyTransactionEntity = new HistoryTransactionEntity();
        historyTransactionEntity.txReversed = history.Txid;
        historyTransactionEntity.isReceived = ElaDataUtils.isReceived(history.Type);
        historyTransactionEntity.fromAddress = ElaDataUtils.isReceived(history.Type) ? history.Inputs.get(0) : history.Outputs.get(0);
        historyTransactionEntity.toAddress = ElaDataUtils.isReceived(history.Type) ? history.Inputs.get(0) : history.Outputs.get(0);
        historyTransactionEntity.fee = new BigDecimal(history.Fee).longValue();
        historyTransactionEntity.blockHeight = history.Height;
        historyTransactionEntity.hash = history.Txid.getBytes();
        historyTransactionEntity.txSize = 0;
        historyTransactionEntity.amount = ElaDataUtils.isReceived(history.Type) ? new BigDecimal(history.Value).longValue() : new BigDecimal(history.Value).subtract(new BigDecimal(history.Fee)).longValue();
        historyTransactionEntity.balanceAfterTx = 0;
        historyTransactionEntity.isValid = true;
        historyTransactionEntity.isVote = !ElaDataUtils.isReceived(history.Type) && ElaDataUtils.isVote(history.TxType);
        historyTransactionEntity.pageNumber = pageNumber;
        historyTransactionEntity.timeStamp = new BigDecimal(history.CreateTime).longValue();
        historyTransactionEntity.memo = ElaDataUtils.getMeno(history.Memo);
        historyTransactionEntity.status=  history.Status;
        historyTransactionEntity.type = history.Type;
        historyTransactionEntity.txType = history.TxType;

        return historyTransactionEntity;
    }

    //true is receive
    public static boolean isReceived(String type){
        if(StringUtil.isNullOrEmpty(type)) return false;
        if(type.equals("spend")) return false;
        if(type.equals("income")) return true;

        return true;
    }

    public static boolean isVote(String type){
        if(!StringUtil.isNullOrEmpty(type)){
            if(type.equalsIgnoreCase("Vote")) return true;
        }
        return false;
    }

    public static boolean checkTx(Context context, String inputAddress, String outputAddress, long amount, List<ElaTransaction> elaTransactions) {
        if(StringUtil.isNullOrEmpty(inputAddress) ||
                StringUtil.isNullOrEmpty(outputAddress) ||
                amount<0 ||
                elaTransactions == null) return false;

        String nodeAddress = null;
        long sumToAmount = 0;
        boolean isSendToOther = false;

        for(ElaTransaction elaTransaction : elaTransactions){
            if(checkSignature(context, elaTransaction)) return false;

            if(elaTransaction.Postmark != null) {
                nodeAddress = ElastosKeypair.getAddress(elaTransaction.Postmark.pub);
                String rewardAddress = ElaDataSource.getInstance(context).getRewardAddress();
                if(!StringUtil.isNullOrEmpty(nodeAddress) && !nodeAddress.equals(rewardAddress)) {
                    return false;
                }
            }

            boolean hasToAddress = false;
            boolean hasNodeAddress = false;
            for(ElaOutput output : elaTransaction.Outputs){

                if(outputAddress.equals(inputAddress)){

                    if(output.address.equals(nodeAddress)){

                        if(output.amount+elaTransaction.Fee != elaTransaction.Total_Node_Fee) {
                            return false;
                        }

                        if(hasNodeAddress){
                            return false;
                        }
                        hasNodeAddress = true;
                    } else {
                        if(!inputAddress.equals(output.address)){
                            return false;
                        }
                    }
                } else {
                    isSendToOther = true;
                    if(!outputAddress.equals(nodeAddress)){
                        if(output.address.equals(nodeAddress)){
                            if(output.amount+elaTransaction.Fee != elaTransaction.Total_Node_Fee){
                                return false;
                            }
                            if(hasNodeAddress){
                                return false;
                            }
                            hasNodeAddress = false;
                        } else if(output.address.equals(outputAddress)){
                            sumToAmount += output.amount;
                            if(hasToAddress) {
                                return false;
                            }
                            hasToAddress = true;
                        } else if(!output.address.equals(inputAddress)){
                            return false;
                        }
                    } else {
                        if(output.address.equals(nodeAddress)){
                            if(output.amount+elaTransaction.Fee == elaTransaction.Total_Node_Fee) {
                                if(hasNodeAddress) {
                                    return false;
                                }
                                hasNodeAddress = true;
                            } else {
                                sumToAmount += output.amount;
                                if(hasToAddress) {
                                    return false;
                                }
                                hasToAddress = true;
                            }
                        } else if(!output.address.equals(inputAddress) ){ //找零地址
                            return false;
                        }
                    }
                }
            }
        }

        if(sumToAmount!=amount && isSendToOther) {
            return false;
        }

        return true;
    }

    private static boolean checkSignature(Context context, ElaTransaction tx){
        if(tx == null) return false;

        String pub = tx.Postmark.pub;
        String signature = tx.Postmark.signature;
        if(!StringUtil.isNullOrEmpty(pub) && !StringUtil.isNullOrEmpty(signature)) {
            StringBuilder sourceBuilder = new StringBuilder();
            for(ElaUTXOInput input : tx.UTXOInputs) {
                sourceBuilder.append(input.txid)
                        .append("-")
                        .append(input.index)
                        .append(";");
            }
            sourceBuilder.append("&");

            for(ElaOutput output : tx.Outputs) {
                sourceBuilder.append(output.address)
                        .append("-")
                        .append(output.amount);
            }
            sourceBuilder.append("&");

            sourceBuilder.append(tx.Fee);

            String source = sourceBuilder.toString();
            boolean isValid = Utility.getInstance(context).verify(pub, source.getBytes(), HexUtils.hexToByteArray(signature));
            return isValid;
        }

        return false;
    }
}
