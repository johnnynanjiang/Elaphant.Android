package org.chat.lib.adapter.holder;

import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import org.chat.lib.R;
import org.chat.lib.adapter.ChatAdapter;
import org.chat.lib.entity.MessageInfo;
import org.chat.lib.utils.Constants;
import org.chat.lib.utils.Utils;
import org.chat.lib.widget.BubbleImageView;
import org.chat.lib.widget.GifTextView;
import org.easy.recycleview.adapter.BaseViewHolder;

public class ChatSendViewHolder extends BaseViewHolder<MessageInfo> {

    TextView chatItemDate;
    ImageView chatItemHeader;
    GifTextView chatItemContentText;
    BubbleImageView chatItemContentImage;
    ImageView chatItemFail;
    ProgressBar chatItemProgress;
    ImageView chatItemVoice;
    LinearLayout chatItemLayoutContent;
    TextView chatItemVoiceTime;
    private ChatAdapter.onItemClickListener onItemClickListener;
    private Handler handler;

    private void initView(View rootView) {
        chatItemDate = rootView.findViewById(R.id.chat_item_date);
        chatItemHeader = rootView.findViewById(R.id.chat_item_header);
        chatItemContentText = rootView.findViewById(R.id.chat_item_content_text);
        chatItemFail = rootView.findViewById(R.id.chat_item_fail);
        chatItemContentImage = rootView.findViewById(R.id.chat_item_content_image);
        chatItemProgress = rootView.findViewById(R.id.chat_item_progress);
        chatItemVoice = rootView.findViewById(R.id.chat_item_voice);
        chatItemLayoutContent = rootView.findViewById(R.id.chat_item_layout_content);
        chatItemVoiceTime = rootView.findViewById(R.id.chat_item_voice_time);
    }

    public ChatSendViewHolder(ViewGroup parent, ChatAdapter.onItemClickListener onItemClickListener, Handler handler) {
        super(parent, R.layout.item_chat_send);
        initView(itemView);
        this.onItemClickListener = onItemClickListener;
        this.handler = handler;
    }


    @Override
    public void setData(MessageInfo data) {
        chatItemDate.setText(data.getTime() != null ? data.getTime() : "");
        Glide.with(getContext()).load(data.getHeader()).into(chatItemHeader);
        chatItemHeader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemClickListener.onHeaderClick(getDataPosition());
            }
        });
        if (data.getContent() != null) {
            chatItemContentText.setSpanText(handler, data.getContent(), true);
            chatItemVoice.setVisibility(View.GONE);
            chatItemContentText.setVisibility(View.VISIBLE);
            chatItemLayoutContent.setVisibility(View.VISIBLE);
            chatItemVoiceTime.setVisibility(View.GONE);
            chatItemContentImage.setVisibility(View.GONE);
        } else if (data.getImageUrl() != null) {
            chatItemVoice.setVisibility(View.GONE);
            chatItemLayoutContent.setVisibility(View.GONE);
            chatItemVoiceTime.setVisibility(View.GONE);
            chatItemContentText.setVisibility(View.GONE);
            chatItemContentImage.setVisibility(View.VISIBLE);
            Glide.with(getContext()).load(data.getImageUrl()).into(chatItemContentImage);
            chatItemContentImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onItemClickListener.onImageClick(chatItemContentImage, getDataPosition());
                }
            });
        } else if (data.getFilepath() != null) {
            chatItemVoice.setVisibility(View.VISIBLE);
            chatItemLayoutContent.setVisibility(View.VISIBLE);
            chatItemContentText.setVisibility(View.GONE);
            chatItemVoiceTime.setVisibility(View.VISIBLE);
            chatItemContentImage.setVisibility(View.GONE);
            chatItemVoiceTime.setText(Utils.formatTime(data.getVoiceTime()));
            chatItemLayoutContent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onItemClickListener.onVoiceClick(chatItemVoice, getDataPosition());
                }
            });
        }
        switch (data.getSendState()) {
            case Constants.CHAT_ITEM_SENDING:
                chatItemProgress.setVisibility(View.VISIBLE);
                chatItemFail.setVisibility(View.GONE);
                break;
            case Constants.CHAT_ITEM_SEND_ERROR:
                chatItemProgress.setVisibility(View.GONE);
                chatItemFail.setVisibility(View.VISIBLE);
                break;
            case Constants.CHAT_ITEM_SEND_SUCCESS:
                chatItemProgress.setVisibility(View.GONE);
                chatItemFail.setVisibility(View.GONE);
                break;
        }
    }
}
