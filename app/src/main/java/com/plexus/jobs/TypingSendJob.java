package com.plexus.jobs;

import androidx.annotation.NonNull;

import com.plexus.jobmanagers.BaseJob;
import com.plexus.jobmanagers.Data;
import com.plexus.jobmanagers.Job;
import com.plexus.jobmanagers.impl.NetworkConstraint;
import com.plexus.utils.logging.Log;

import java.util.concurrent.TimeUnit;

public class TypingSendJob extends BaseJob {

    public static final String KEY = "TypingSendJob";

    private static final String TAG = TypingSendJob.class.getSimpleName();

    private static final String KEY_THREAD_ID = "thread_id";
    private static final String KEY_TYPING = "typing";

    private long threadId;
    private boolean typing;

    public TypingSendJob(long threadId, boolean typing) {
        this(new Job.Parameters.Builder()
                        .setQueue(getQueue(threadId))
                        .setMaxAttempts(1)
                        .setLifespan(TimeUnit.SECONDS.toMillis(5))
                        .addConstraint(NetworkConstraint.KEY)
                        .setMemoryOnly(true)
                        .build(),
                threadId,
                typing);
    }

    private TypingSendJob(@NonNull Job.Parameters parameters, long threadId, boolean typing) {
        super(parameters);

        this.threadId = threadId;
        this.typing = typing;
    }

    public static String getQueue(long threadId) {
        return "TYPING_" + threadId;
    }

    @Override
    public @NonNull
    Data serialize() {
        return new Data.Builder().putLong(KEY_THREAD_ID, threadId)
                .putBoolean(KEY_TYPING, typing)
                .build();
    }

    @Override
    public @NonNull
    String getFactoryKey() {
        return KEY;
    }

    @Override
    public void onRun() throws Exception {


        Log.d(TAG, "Sending typing " + (typing ? "started" : "stopped") + " for thread " + threadId);

        /*Recipient recipient = DatabaseFactory.getThreadDatabase(context).getRecipientForThreadId(threadId);

        if (recipient == null) {
            Log.w(TAG, "Tried to send a typing indicator to a non-existent thread.");
            return;
        }

        if (recipient.isBlocked()) {
            Log.w(TAG, "Not sending typing indicators to blocked recipients.");
        }

        List<Recipient>  recipients = Collections.singletonList(recipient);
        Optional<byte[]> groupId    = Optional.absent();

        if (recipient.isGroup()) {
            recipients = DatabaseFactory.getGroupDatabase(context).getGroupMembers(recipient.requireGroupId(), GroupDatabase.MemberSet.FULL_MEMBERS_EXCLUDING_SELF);
            groupId    = Optional.of(recipient.requireGroupId().getDecodedId());
        }

        recipients = RecipientUtil.getEligibleForSending(Stream.of(recipients)
                .map(Recipient::resolve)
                .toList());

        SignalServiceMessageSender             messageSender      = ApplicationDependencies.getSignalServiceMessageSender();
        List<SignalServiceAddress>             addresses          = RecipientUtil.toSignalServiceAddressesFromResolved(context, recipients);
        List<Optional<UnidentifiedAccessPair>> unidentifiedAccess = UnidentifiedAccessUtil.getAccessFor(context, recipients);
        SignalServiceTypingMessage             typingMessage      = new SignalServiceTypingMessage(typing ? Action.STARTED : Action.STOPPED, System.currentTimeMillis(), groupId);

        if (isCanceled()) {
            Log.w(TAG, "Canceled before send!");
            return;
        }

        try {
            messageSender.sendTyping(addresses, unidentifiedAccess, typingMessage, this::isCanceled);
        } catch (CancelationException e) {
            Log.w(TAG, "Canceled during send!");
        }*/
    }

    @Override
    public void onFailure() {
    }

    @Override
    protected boolean onShouldRetry(@NonNull Exception exception) {
        return false;
    }

    public static final class Factory implements Job.Factory<TypingSendJob> {
        @Override
        public @NonNull
        TypingSendJob create(@NonNull Parameters parameters, @NonNull Data data) {
            return new TypingSendJob(parameters, data.getLong(KEY_THREAD_ID), data.getBoolean(KEY_TYPING));
        }
    }

}
