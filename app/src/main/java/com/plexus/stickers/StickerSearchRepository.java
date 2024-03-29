package com.plexus.stickers;

import android.content.Context;
import android.database.Cursor;

import androidx.annotation.NonNull;

import com.plexus.components.emoji.EmojiUtil;
import com.plexus.core.utils.concurrent.PlexusExecutors;
import com.plexus.database.AttachmentDatabase;
import com.plexus.database.CursorList;
import com.plexus.database.DatabaseFactory;
import com.plexus.database.StickerDatabase;
import com.plexus.database.model.StickerRecord;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.plexus.database.StickerDatabase.*;

public final class StickerSearchRepository {

    private final StickerDatabase stickerDatabase;
    private final AttachmentDatabase attachmentDatabase;

    public StickerSearchRepository(@NonNull Context context) {
        this.stickerDatabase    = DatabaseFactory.getStickerDatabase(context);
        this.attachmentDatabase = DatabaseFactory.getAttachmentDatabase(context);
    }

    public void searchByEmoji(@NonNull String emoji, @NonNull Callback<List<StickerRecord>> callback) {
        PlexusExecutors.BOUNDED.execute(() -> {
            String              searchEmoji = EmojiUtil.getCanonicalRepresentation(emoji);
            List<StickerRecord> out         = new ArrayList<>();
            Set<String>         possible    = EmojiUtil.getAllRepresentations(searchEmoji);

            for (String candidate : possible) {
                try (StickerRecordReader reader = new StickerRecordReader(stickerDatabase.getStickersByEmoji(candidate))) {
                    StickerRecord record = null;
                    while ((record = reader.getNext()) != null) {
                        out.add(record);
                    }
                }
            }

            callback.onResult(out);
        });
    }

    public void getStickerFeatureAvailability(@NonNull Callback<Boolean> callback) {
        PlexusExecutors.BOUNDED.execute(() -> {
            try (Cursor cursor = stickerDatabase.getAllStickerPacks("1")) {
                if (cursor != null && cursor.moveToFirst()) {
                    callback.onResult(true);
                } else {
                    callback.onResult(attachmentDatabase.hasStickerAttachments());
                }
            }
        });
    }

    private static class StickerModelBuilder implements CursorList.ModelBuilder<StickerRecord> {
        @Override
        public StickerRecord build(@NonNull Cursor cursor) {
            return new StickerRecordReader(cursor).getCurrent();
        }
    }

    public interface Callback<T> {
        void onResult(T result);
    }
}
