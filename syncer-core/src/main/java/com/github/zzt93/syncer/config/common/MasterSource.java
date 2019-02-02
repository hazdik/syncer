package com.github.zzt93.syncer.config.common;

import com.github.zzt93.syncer.common.data.SyncInitMeta;
import com.github.zzt93.syncer.config.consumer.input.MasterSourceType;
import com.github.zzt93.syncer.config.consumer.input.Repo;
import com.github.zzt93.syncer.config.consumer.input.SyncMeta;
import com.github.zzt93.syncer.consumer.ConsumerSource;
import com.github.zzt93.syncer.consumer.input.EventScheduler;
import com.github.zzt93.syncer.consumer.input.LocalConsumerSource;
import com.github.zzt93.syncer.consumer.input.MongoLocalConsumerSource;
import com.github.zzt93.syncer.consumer.input.MysqlLocalConsumerSource;
import com.github.zzt93.syncer.consumer.input.SchedulerBuilder.SchedulerType;
import com.github.zzt93.syncer.producer.input.mongo.DocTimestamp;
import com.github.zzt93.syncer.producer.input.mysql.connect.BinlogInfo;
import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * @author zzt
 */
public class MasterSource {

  private final Logger logger = LoggerFactory.getLogger(MasterSource.class);
  private final Set<Repo> repoSet = new HashSet<>();
  private MasterSourceType type = MasterSourceType.MySQL;
  private SchedulerType scheduler = SchedulerType.hash;
  private SyncMeta syncMeta;
  private ClusterConnection connection;
  private List<Repo> repos = new ArrayList<>();

  public ClusterConnection getConnection() {
    return connection;
  }

  public void setConnection(ClusterConnection connection) {
    this.connection = connection;
  }

  public List<Repo> getRepos() {
    return repos;
  }

  public void setRepos(List<Repo> repos) {
    this.repos = repos;
    repoSet.addAll(repos);
    if (repoSet.size() < repos.size()) {
      logger.error("Duplicate repos: {}", repos);
      throw new InvalidConfigException("Duplicate repos");
    }
  }

  public Set<Repo> getRepoSet() {
    return repoSet;
  }

  public MasterSourceType getType() {
    return type;
  }

  public void setType(MasterSourceType type) {
    this.type = type;
  }

  public void setSyncMeta(SyncMeta syncMeta) {
    this.syncMeta = syncMeta;
  }

  public boolean hasSyncMeta() {
    return syncMeta != null && type == MasterSourceType.MySQL;
  }

  public SyncMeta getSyncMeta() {
    return syncMeta;
  }

  public void setScheduler(SchedulerType scheduler) {
    this.scheduler = scheduler;
  }

  public SchedulerType getScheduler() {
    return scheduler;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    MasterSource that = (MasterSource) o;

    return connection.equals(that.connection);
  }

  @Override
  public int hashCode() {
    return connection.hashCode();
  }

  @Override
  public String toString() {
    return "MasterSource{" +
        "connection=" + connection +
        ", repos=" + repos +
        ", type=" + type +
        '}';
  }

  public List<String> remoteIds() {
    return connection.remoteIds();
  }

  public List<? extends ConsumerSource> toConsumerSources(String consumerId,
                                                          HashMap<String, SyncInitMeta> id2SyncInitMeta,
                                                          EventScheduler scheduler) {
    List<LocalConsumerSource> res = new LinkedList<>();
    ClusterConnection cluster = getConnection();
    for (int i = 0; i < cluster.getConnections().size(); i++) {
      Connection connection = cluster.getConnections().get(i);
      SyncInitMeta syncInitMeta = getSyncInitMeta(cluster.getSyncMetas().get(i), id2SyncInitMeta, connection);
      switch (getType()) {
        case Mongo:
          Preconditions
              .checkState(syncInitMeta instanceof DocTimestamp, "syncInitMeta is " + syncInitMeta);
          res.add(new MongoLocalConsumerSource(consumerId, connection,
              getRepoSet(), (DocTimestamp) syncInitMeta, scheduler));
          break;
        case MySQL:
          Preconditions
              .checkState(syncInitMeta instanceof BinlogInfo, "syncInitMeta is " + syncInitMeta);
          res.add(new MysqlLocalConsumerSource(consumerId, connection,
              getRepoSet(), (BinlogInfo) syncInitMeta, scheduler));
          break;
        default:
          throw new IllegalStateException("Not implemented type");
      }
    }
    return res;
  }

  private SyncInitMeta getSyncInitMeta(SyncMeta syncMeta, HashMap<String, SyncInitMeta> id2SyncInitMeta, Connection connection) {
    String identifier = connection.connectionIdentifier();
    SyncInitMeta syncInitMeta = id2SyncInitMeta.get(identifier);
    if (syncMeta != null) {
      logger.warn("Override syncer remembered position with config in file {}, watch out", syncMeta);
      syncInitMeta = BinlogInfo.withFilenameCheck(syncMeta.getBinlogFilename(), syncMeta.getBinlogPosition());
    }
    return syncInitMeta;
  }
}
