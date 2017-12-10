package me.sashie.skriptyaml;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;

import javax.annotation.Nullable;

import org.bukkit.event.Event;

import ch.njol.skript.lang.TriggerItem;

/**
 * Effects that extend this class are ran asynchronously. Next trigger item will
 * be ran in main server thread, as if there had been a delay before.
 * <p>
 * Majority of Skript and Minecraft APIs are not thread-safe, so be careful.
 */
public abstract class AsyncEffect extends DelayFork {

	private static final ReentrantLock SKRIPT_EXECUTION = new ReentrantLock(true);
	private static final ExecutorService THREADS = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
	
	@Override
	@Nullable
	protected TriggerItem walk(Event e) {
		debug(e, true);
		DelayFork.addDelayedEvent(e);
		CompletableFuture<Void> run = CompletableFuture.runAsync(new Runnable() {
			public void run() {
				execute(e);
			}
		}, THREADS);
		run.whenComplete((r, err) -> {
		      if (err != null) {
		          err.printStackTrace();
		      }
		      SKRIPT_EXECUTION.lock();
				try {
					if (getNext() != null) {
						walk(getNext(), e);
					}
				} finally {
					SKRIPT_EXECUTION.unlock();
				}
		});
		return null;
	}
}