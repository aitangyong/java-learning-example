package opensource.guava.collect;

import com.google.common.base.Splitter;
import com.google.common.collect.*;
import org.junit.Test;

import java.util.List;

public class TestNewCollection {

    @Test
    public void biMap() {
        // 实现键值对的双向映射
        // preserves the uniqueness of its values as well as that of its keys
        BiMap<Integer, String> biMap = HashBiMap.create();
        biMap.put(0, "monday");
        System.out.println(biMap.inverse().get("monday"));
    }

    @Test
    public void multiset() {
        String strWorld = "a|b|b|a|c|b|e";
        List<String> wordList = Splitter.on("|").splitToList(strWorld);
        Multiset<String> wordsMultiset = HashMultiset.create();
        wordsMultiset.addAll(wordList);
        for (String key : wordsMultiset.elementSet()) {
            System.out.println("word=" + key + ",count=" + wordsMultiset.count(key));
        }
    }

    @Test
    public void multimap() {
        Multimap<String, String> multimap = HashMultimap.create();
        multimap.put("id", "0");
        multimap.put("id", "1");
        multimap.put("id", "2");
        System.out.println(multimap.get("id"));
    }

    @Test
    public void table() {
        // <row,column,value>
        Table<String, String, String> employeeTable = HashBasedTable.create();
        employeeTable.put("IBM", "101", "Mahesh");
        employeeTable.put("IBM", "102", "Ramesh");
        employeeTable.put("IBM", "103", "Suresh");

        employeeTable.put("Microsoft", "111", "Sohan");
        employeeTable.put("Microsoft", "112", "Mohan");
        employeeTable.put("Microsoft", "113", "Rohan");

        employeeTable.put("TCS", "121", "Ram");
        employeeTable.put("TCS", "102", "Shyam");
        employeeTable.put("TCS", "123", "Sunil");

        // 返回所有row(所有公司)
        System.out.println(employeeTable.rowKeySet());

        // 返回所有column(所有员工编号)
        System.out.println(employeeTable.columnKeySet());

        // 返回所有value(所有员工名称)
        System.out.println(employeeTable.values());

        // company->(id->name)
        System.out.println(employeeTable.rowMap());

        // id->(company->name)
        System.out.println(employeeTable.columnMap());

        // row+column对应的value
        System.out.println(employeeTable.get("IBM", "101"));
    }

}
