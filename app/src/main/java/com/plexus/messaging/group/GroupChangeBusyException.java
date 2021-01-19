package com.plexus.messaging.group;

import androidx.annotation.NonNull;

public final class GroupChangeBusyException extends GroupChangeException {

    public GroupChangeBusyException(@NonNull Throwable throwable) {
        super(throwable);
    }

    public GroupChangeBusyException(@NonNull String message) {
        super(message);
    }
}
