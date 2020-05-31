package opensource.apache;

import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.junit.Assert;
import org.junit.Test;

public class TestCaseInsensitiveMap {

    @Test
    public void t() {
        CaseInsensitiveMap<String, String> caseInsensitiveMap = new CaseInsensitiveMap<>();
        caseInsensitiveMap.put("aB", "100");
        Assert.assertEquals("100", caseInsensitiveMap.get("Ab"));
    }
}
