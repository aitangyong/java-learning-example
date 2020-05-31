package opensource.guava.collect;

import com.google.common.collect.Sets;
import org.junit.Test;

import java.util.List;
import java.util.Set;

public class TestSets {

    @Test
    public void difference_intersection_union() {
        Set<Integer> set1 = Sets.newHashSet(1, 2, 3, 4);
        Set<Integer> set2 = Sets.newHashSet(3, 4, 5, 6);

        // set1 - set2
        System.out.println(Sets.difference(set1, set2));

        // set1 & set2
        System.out.println(Sets.intersection(set1, set2));

        // set1 | set2
        System.out.println(Sets.union(set1, set2));

        // (set1 | set2) - (set1 & set2)
        System.out.println(Sets.symmetricDifference(set1, set2));
    }

    @Test
    public void cartesianProduct() {
        Set<Integer> set1 = Sets.newHashSet(1, 2, 3, 4);
        Set<Integer> set2 = Sets.newHashSet(3, 4, 5, 6);
        Set<Integer> set3 = Sets.newHashSet(8, 9);

        Set<List<Integer>> cartesian = Sets.cartesianProduct(set1, set2, set3);
        cartesian.forEach(System.out::println);
    }

    @Test
    public void combinations() {
        Set<Integer> set = Sets.newHashSet(1, 2, 3, 4);
        Set<Set<Integer>> combinations = Sets.combinations(set, 2);
        combinations.forEach(System.out::println);
    }
}
