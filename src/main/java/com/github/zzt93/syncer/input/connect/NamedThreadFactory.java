package com.github.zzt93.syncer.input.connect;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author zzt
 */
public class NamedThreadFactory implements ThreadFactory {

  private static final AtomicLong count = new AtomicLong(1);
  private static Logger logger = LoggerFactory.getLogger(NamedThreadFactory.class);
  private final String prefix;

  public NamedThreadFactory() {
    prefix = "syncer-tmp";
  }

  public NamedThreadFactory(String prefix) {
    this.prefix = prefix;
  }

  @Override
  public Thread newThread(Runnable r) {
    logger.debug("Create a new thread", count);
    return new Thread(r, prefix + "-" + count.getAndAdd(1L));
  }
}
