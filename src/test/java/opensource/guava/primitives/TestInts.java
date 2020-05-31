package opensource.guava.primitives;

import com.google.common.primitives.Ints;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

public class TestInts {

    @Test
    public void max_min() {
        Assert.assertEquals(5, Ints.max(1, 2, 3, 4, 5));
        Assert.assertEquals(1, Ints.min(1, 2, 3, 4, 5));
    }

    @Test
    public void concat() {
        int[] array1 = {1, 2, 3};
        int[] array2 = {3, 2, 1};
        int[] all = Ints.concat(array1, array2);
        System.out.println(Arrays.toString(all));
    }
}
