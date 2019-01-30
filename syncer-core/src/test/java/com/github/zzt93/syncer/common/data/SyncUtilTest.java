package com.github.zzt93.syncer.common.data;

import com.github.zzt93.syncer.data.util.SyncUtil;
import com.google.gson.reflect.TypeToken;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.Map;

/**
 * @author zzt
 */
public class SyncUtilTest {

  @Test
  public void fromJson() throws Exception {
    Object o = SyncUtil.fromJson("[\"AC\",\"BD\",\"CE\",\"DF\",\"GG\"]", String[].class);
    Assert.assertEquals(o.getClass(), String[].class);
    String[] ss = (String[]) o;
    Assert.assertEquals(ss.length, 5);
    Object map = SyncUtil.fromJson("{a:1, b:\"asd\"}", Map.class);
    Map m = (Map) map;
    Assert.assertEquals(m.size(),2);
    Assert.assertEquals(m.get("a"),1.0);
    Assert.assertEquals(m.get("b"),"asd");
  }

  @Test
  public void announcementBlock() throws Exception {
    Map o = (Map) SyncUtil.fromJson(
        "{\"blocks\":[{\"data\":{},\"depth\":0,\"entityRanges\":[],\"inlineStyleRanges\":[],\"key\":\"ummxd\",\"text\":\"Test\",\"type\":\"unstyled\"}],\"entityMap\":{}}",
        Map.class);
    List<Map> blocks = ((List<Map>) o.get("blocks"));
    Assert.assertEquals(blocks.size(), 1);
    Map map = blocks.get(0);
    String text = (String) map.get("text");
    Assert.assertEquals(text, "Test");
  }

  @Test
  public void testWithToken() {
    Map<String, String> map = SyncUtil.fromJson("{ \"_id\" : \"EuFB$vwXpMPb\", \"_key\" : \"L76y$\", \"tp\" : 3, \"sub\" : 0, \"name\" : \"测试-熊大飞\", \"time\" : 1547535531778, \"fromUserId\" : NumberLong(260404), \"index\" : 95, \"content\" : \"{\\\"code\\\":2500,\\\"data\\\":\\\"Duplicate key cn.superid.live.form.StartLiveForm$StreamLayout@44f3346f\\\"}\", \"groupId\" : NumberLong(12143021), \"fromRoleId\" : NumberLong(13000005), \"apns\" : \"[]\", \"state\" : 0, \"options\" : \"{\\\"announcementId\\\":13150624}\" }", new TypeToken<Map<String, String>>() {
    });
    Assert.assertTrue(map.containsKey("content"));
  }

}