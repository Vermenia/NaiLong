package com.vermenia.nailong.ai;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * A bounded conversation context. Persisting it per player and per Nailong is deliberately deferred
 * until the dialogue actions are approved; keeping the limit here prevents an eventual save from
 * growing without bound.
 */
public final class NailongMemory {
    private static final int MAX_TURNS = 100;
    private final Deque<Turn> turns = new ArrayDeque<>();

    public void remember(String playerMessage, String nailongReply) {
        turns.addLast(new Turn(playerMessage, nailongReply));
        while (turns.size() > MAX_TURNS) {
            turns.removeFirst();
        }
    }

    public Deque<Turn> recentTurns() {
        return new ArrayDeque<>(turns);
    }

    public record Turn(String playerMessage, String nailongReply) {
    }
}
