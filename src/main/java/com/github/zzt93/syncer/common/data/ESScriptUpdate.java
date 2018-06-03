package com.github.zzt93.syncer.common.data;

import com.github.shyiko.mysql.binlog.event.EventType;
import java.util.HashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @see ExtraQuery
 * @see SyncByQuery
 * https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-update.html#_scripted_updates
 */
public class ESScriptUpdate extends SyncByQuery {

  private static final Logger logger = LoggerFactory.getLogger(ESScriptUpdate.class);

  // todo other script op: +=, contains
  private final HashMap<String, Object> append = new HashMap<>();
  private final HashMap<String, Object> remove = new HashMap<>();
  private transient final SyncData outer;

  public ESScriptUpdate(SyncData data) {
    outer = data;
  }

  public ESScriptUpdate updateList(String listField, Object delta) {
    switch (outer.getType()) {
      case DELETE_ROWS:
        remove.put(listField, delta);
        break;
      case WRITE_ROWS:
        append.put(listField, delta);
        break;
      default:
        logger.warn("Not support update list variable for {}", outer.getType());
    }
    outer.setEventType(EventType.UPDATE_ROWS);
    return this;
  }

  public boolean needScript() {
    return !append.isEmpty() || !remove.isEmpty();
  }

  public HashMap<String, Object> getAppend() {
    return append;
  }

  public HashMap<String, Object> getRemove() {
    return remove;
  }

  @Override
  public String toString() {
    return "SyncByQueryES{" +
        ", append=" + append +
        ", remove=" + remove +
        ", outer=SyncData@" + Integer.toHexString(outer.hashCode()) +
        '}';
  }
}