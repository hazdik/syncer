package com.github.zzt93.syncer.consumer.input;

import com.github.zzt93.syncer.common.data.SyncData;
import com.github.zzt93.syncer.common.data.SyncInitMeta;
import com.github.zzt93.syncer.config.pipeline.input.MasterSource;
import com.github.zzt93.syncer.config.pipeline.input.MasterSourceType;
import com.github.zzt93.syncer.consumer.InputSource;
import com.github.zzt93.syncer.producer.input.mongo.DocId;
import com.github.zzt93.syncer.producer.input.mysql.connect.BinlogInfo;
import com.github.zzt93.syncer.producer.register.ConsumerRegistry;
import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingDeque;

/**
 * @author zzt
 */
public class Registrant {

  private final List<InputSource> inputSources = new ArrayList<>();
  private final ConsumerRegistry consumerRegistry;
  private final String clientId;
  private final BlockingDeque<SyncData> filterInput;

  Registrant(String clientId, ConsumerRegistry consumerRegistry,
      BlockingDeque<SyncData> filterInput) {
    this.consumerRegistry = consumerRegistry;
    this.clientId = clientId;
    this.filterInput = filterInput;
  }

  Boolean register() {
    boolean res= true;
    for (InputSource inputSource : inputSources) {
      res = res && consumerRegistry.register(inputSource.getRemoteConnection(), inputSource);
    }
    return res;
  }

  void addDatasource(MasterSource masterSource, SyncInitMeta syncInitMeta,
      MasterSourceType sourceType) {
    LocalInputSource inputSource = null;
    switch (sourceType) {
      case MONGO:
        Preconditions.checkState(syncInitMeta instanceof DocId);
        inputSource = new MongoLocalInputSource(clientId,masterSource.getConnection(),masterSource.getSchemaSet(),
            (DocId) syncInitMeta,filterInput);
        break;
      case MYSQL:
        Preconditions.checkState(syncInitMeta instanceof BinlogInfo);
        inputSource = new MySQLLocalInputSource(
            clientId, masterSource.getConnection(), masterSource.getSchemaSet(),
            (BinlogInfo) syncInitMeta, filterInput);
    }
    inputSources.add(inputSource);
  }

}
