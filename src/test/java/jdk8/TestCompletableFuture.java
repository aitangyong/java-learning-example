package jdk8;

import com.google.common.base.Stopwatch;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.junit.*;

import java.util.Random;
import java.util.concurrent.*;
import java.util.function.*;

/**
 * https://www.cnblogs.com/txmfz/p/11266411.html
 * <pre>
 * <strong>产出型</strong>
 * 用上一个阶段的结果作为指定函数的参数执行函数产生新的结果。
 * 这一类接口方法名中基本都有apply字样,接口的参数是(Bi)Function类型
 *
 * <strong>消费型</strong>
 * 用上一个阶段的结果作为指定操作的参数执行指定的操作，但不对阶段结果产生影响。
 * 这一类接口方法名中基本都有accept字样，接口的参数是(Bi)Consumer类型
 *
 * <strong>不消费也不产出型</strong>
 * 不依据上一个阶段的执行结果，只要上一个阶段完成（但一般要求正常完成），就执行指定的操作，且不对阶段的结果产生影响。
 * 这一类接口方法名中基本都有run字样，接口的参数是Runnable类型
 *
 * 注意: 还有一组特别的方法带有compose字样，它以依赖阶段本身作为参数而不是阶段产生的结果进行产出型
 * </pre>
 *
 * <pre>
 * 多阶段的依赖：一个阶段的执行可以由一个阶段的完成触发，或者两个阶段的同时完成，或者两个阶段中的任何一个完成
 * 1.方法前缀为then的方法安排了对单个阶段的依赖。
 * 2.那些由完成两个阶段而触发的，可以结合他们的结果或产生的影响，这一类方法带有combine或者both字样。
 * 3.那些由两个阶段中任意一个完成触发的，不能保证哪个的结果或效果用于相关阶段的计算，这类方法带有either字样。
 * </pre>
 *
 * <pre>
 * 按执行的方式：阶段之间的依赖关系控制计算的触发，但不保证任何特定的顺序,因为一个阶段的执行可以采用以下三种方式之一安排
 * 1.默认的执行方式。所有方法名没有以async后缀的方法都按这种默认执行方式执行。
 * 2.默认的异步执行。所有方法名以async为后缀，但没有Executor参数的方法都属于此类。
 * 3.自定义执行方式。所有方法名以async为后缀，并且具有Executor参数的方法都属于此类。
 * </pre>
 *
 * <pre>
 * 1.按上一个阶段的完成状态：无论触发阶段是正常完成还是异常完成都会执行：
 * whenComplete方法可以在上一个阶段不论以何种方式完成的处理，但它是一个消费型接口，即不对整个阶段的结果产生影响。
 * handle前缀的方法也可以在上一个阶段不论以何种方式完成的处理，它是一个产出型接口，既可以由上一个阶段的异常产出新结果，
 * 也可以其正常结果产出新结果，使该结果可以由其他相关阶段继续进一步处理。
 *
 * 2.上一个阶段是异常完成的时候执行：
 * exceptionally方法可以在上一个阶段以异常完成时进行处理，它可以根据上一个阶段的异常产出新的结果，使该结果可以由其他相关阶段继续进一步处理。
 * </pre>
 *
 * <pre>
 * 除了whenComplete不要求其依赖的阶段是正常完成还是异常完成，以及handle方法只要求其依赖的阶段异常完成之外，其余所有接口方法都要求其依赖的阶段正常完成。
 * 1.如果一个阶段的执行由于一个(未捕获的)异常或错误而突然终止，那么所有要求其完成的相关阶段也将异常地完成，并通过CompletionException包装其具体异常堆栈。
 * 2.如果一个阶段同时依赖于两个阶段，并且两个阶段都异常地完成，那么CompletionException可以对应于这两个异常中的任何一个。
 * 3.如果一个阶段依赖于另外两个阶段中的任何一个，并且其中只有一个异常完成，则不能保证依赖阶段是正常完成还是异常完成。
 * 4.在使用方法whenComplete的情况下，当提供的操作本身遇到异常时，如果前面的阶段没有异常完成，则阶段将以其异常作为原因异常完成。
 * </pre>
 */
public class TestCompletableFuture {

    /**
     * isDone: 任务是否正常完成或者异常完成
     * <p>
     * isCompletedExceptionally: 任务是否异常完成
     * <p>
     * isCancelled: 任务是否取消
     */
    @Test
    public void getState() {
        CompletableFuture<Integer> completeFuture = CompletableFuture.completedFuture(1);
        Assert.assertTrue(completeFuture.isDone());
        Assert.assertFalse(completeFuture.isCompletedExceptionally());
        Assert.assertFalse(completeFuture.isCancelled());

        CompletableFuture<Integer> exceptionFuture = CompletableFuture.failedFuture(new NullPointerException());
        Assert.assertTrue(exceptionFuture.isDone());
        Assert.assertTrue(exceptionFuture.isCompletedExceptionally());
        Assert.assertFalse(completeFuture.isCancelled());
    }

    /**
     * complete正常完成, completeExceptionally异常完成, cancel中断任务
     * <p>
     * 状态流转只能从init到这3种状态之一,并且只能流转一次,如果操作返回false则意味着更改状态失败(已经被别人更改过状态了)
     * <p>
     * obtrudeValue和obtrudeException使用这两个方法可以强制地将值设置或者将异常抛出, 无论该之前任务是否完成
     * <p>
     * In most of the cases you should use the method complete or method completeExceptionally.
     * These method are safe and guarantee that the result is set only if the fture is not completed
     * and the result can not be changed by subsequent calling of these methods, so when you call the get()
     * method you get always the first result setted by these methods.
     */
    @Test
    public void controlState() throws Exception {
        // 直接new出来的CompletableFuture对象是无法运行的,因为他并没有处于"完成"状态,也就是说你调用get()方法是会被阻塞的。
        CompletableFuture<Integer> initFuture1 = new CompletableFuture<>();
        initFuture1.complete(10);// 完成
        Assert.assertTrue(initFuture1.isDone());

        // 异常
        CompletableFuture<Integer> initFuture2 = new CompletableFuture<>();
        initFuture2.completeExceptionally(new NullPointerException());
        Assert.assertTrue(initFuture2.isCompletedExceptionally());

        // 取消
        CompletableFuture<Integer> initFuture3 = new CompletableFuture<>();
        initFuture3.cancel(false);
        Assert.assertTrue(initFuture3.isCancelled());

        // obtrudeValue
        CompletableFuture<String> future1 = new CompletableFuture<>();
        future1.complete("test1");
        System.out.println(future1.get()); // test1
        future1.complete("test2");
        System.out.println(future1.get()); // test1 , value not overwritten

        future1.obtrudeValue("obtrudeValue");
        System.out.println(future1.get()); // obtrudeValue , overwrite the value even of completed future
        future1.obtrudeValue("newObtrudeValue");
        System.out.println(future1.get()); // newObtrudeValue , subsequent call overwrite the value

        future1.complete("test3");
        System.out.println(future1.get()); // newObtrudeValue , value not overwritten by calling complete
    }

    /**
     * getNow()该方法不阻塞，如果任务尚未完成，则返回默认值; 如果任务已经完成, 则返回任务的值
     * <p>
     * get()调用时会阻塞当前线程,会抛出编译时异常ExecutionException
     * <p>
     * join()也是阻塞的,只会抛运行时异常; 如果任务被取消get和join都抛出CancellationException
     */
    @Test
    public void getValue() {
        CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return 3;
        });

        // 当前线程不阻塞,任务没完成直接返回默认值
        Assert.assertEquals(0, (int) future.getNow(0));

        // 任务在指定时间内没有完成,抛出TimeoutException
        try {
            future.get(1, TimeUnit.SECONDS);
        } catch (TimeoutException t) {
            Assert.assertTrue(true);
        } catch (InterruptedException | ExecutionException e) {
            // ignore
        }


        // 取消任务
        future.cancel(false);
        try {
            future.join();
        } catch (CancellationException e) {
            Assert.assertTrue(true);
        }
    }

    /**
     * 当任务没有执行完,orTimeout会导致抛出异常,而completeOnTimeout不会抛异常而是设置一个默认值
     */
    @Test
    public void timeout() {
        // orTimeout
        CompletableFuture<Integer> future1 = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return 10;
        }).orTimeout(1, TimeUnit.SECONDS);
        try {
            future1.join();
        } catch (CompletionException e) {
            Assert.assertTrue(e.getCause() instanceof TimeoutException);
        }

        // completeOnTimeout
        CompletableFuture<Integer> future2 = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return 10;
        }).completeOnTimeout(0, 1, TimeUnit.SECONDS);
        Assert.assertEquals(0, (int) future2.join());
    }

    /**
     * allOf返回Void类型的future, allOf会等待future列表中的所有任务执行完毕,任务列表中只要有1个任务抛出异常则,则返回的future就是异常完成状态
     * <p>
     * anyOf返回Object类型,哪儿个future先完成,就返回那个future的结果
     */
    @Test
    public void allOfAnyOf() {
        Stopwatch stopwatch = Stopwatch.createStarted();
        CompletableFuture<Integer> future1 = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(1000);
                throw new NullPointerException();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return 1;
        });
        CompletableFuture<Integer> future2 = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return 2;
        });
        CompletableFuture<Integer> future3 = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return 3;
        });

        CompletableFuture<Void> allFuture = CompletableFuture.allOf(future1, future2, future3);
        try {
            allFuture.join();
        } catch (Exception e) {
            // ignore
        }
        // 不管任务是否异常，这里的耗时都是3s左右
        System.out.println(stopwatch.elapsed(TimeUnit.SECONDS));
        Assert.assertTrue(allFuture.isDone());

        CompletableFuture<Integer> future4 = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(4000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return 4;
        });
        CompletableFuture<Integer> future5 = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return 5;
        });
        CompletableFuture<Object> anyFuture = CompletableFuture.anyOf(future4, future5);
        System.out.println(anyFuture.join());
    }

    @Test
    public void runAsync() throws Exception {
        ExecutorService executorService = new ThreadPoolExecutor(5,
                5,
                60,
                TimeUnit.SECONDS,
                new SynchronousQueue<>(),
                new ThreadFactoryBuilder().setNameFormat("custom-pool-%d").build());
        Runnable runnable = () -> {
            System.out.println(Thread.currentThread().getName());
        };
        CompletableFuture<Void> voidFuture = CompletableFuture.runAsync(runnable, executorService);
        voidFuture.get();

        Supplier<Integer> supplier = () -> {
            System.out.println(Thread.currentThread().getName());
            return 1;
        };
        // 默认使用ForkJoinPool.commonPool()作为执行异步任务的线程池
        CompletableFuture<Integer> intFuture = CompletableFuture.supplyAsync(supplier);
        Assert.assertEquals(1, (int) intFuture.get());
    }

    /**
     * whenComplete和whenCompleteAsync方法(上一阶段的任务正常完成和异常完成)都会被触发调用, 他是一个"消费型"接口,
     * 即不会改变阶段的现状
     * <p>
     * handle和handleAsync方法(上一阶段的任务正常完成和异常完成)都会被触发调用, 他是一个"产出型"接口,
     * 即可以对正常完成的结果进行转换，也可以对异常完成的进行补偿一个结果，即可以改变阶段的现状
     * <p>
     * exceptionally和exceptionallyAsync方法(上一阶段的任务异常完成)才被触发调用,它可以根据上一个阶段的异常产出新的结果，
     * 使该结果可以由其他相关阶段继续进一步处理(补偿上一个阶段的异常)
     */
    @Test
    public void whenComplete() throws Exception {
        // 只能消费上一个阶段的返回结果, 对最终结果不会有影响
        CompletableFuture<Integer> completeFuture = CompletableFuture.supplyAsync(() -> 1)
                .whenComplete((value, e) -> System.out.println("value=" + value + ",e=" + e));
        System.out.println(completeFuture.get());// 1

        CompletableFuture<Integer> handleFuture = CompletableFuture.supplyAsync(() -> 1)
                .handle((t, e) -> 10 * t);
        System.out.println(handleFuture.get());// 10

        // exceptionally只有一个参数e，表示上一个节点的异常，只有上一个阶段异常完成才会被执行
        // 在异常时返回了新的值1对出现异常的阶段进行了补偿，所以最终整个阶段不会出现异常
        CompletableFuture<Object> exceptionFuture = CompletableFuture.supplyAsync(() -> {
            throw new NullPointerException();
        }).exceptionally(e -> 1);
        System.out.println(exceptionFuture.get());// 1
    }

    /**
     * <strong>单阶段依赖</strong>,第2个任务依赖第一个任务, 如果第一个任务异常, 那么第二个任务不再触发
     * <p>
     * thenApply是产出型, thenAccept是消费型, thenRun不产生也不消费
     * <p>
     * thenCompose依赖上一阶段本身而不是结果, thenApply和thenAccept和thenRun都是直接依赖上一阶段的结果
     */
    @Test
    public void thenApply() throws Exception {
        CompletableFuture<Long> future = CompletableFuture.supplyAsync(new Supplier<Long>() {
            @Override
            public Long get() {
                long result = new Random().nextInt(100);
                System.out.println("result1=" + result);
                throw new NullPointerException();
                // return result;
            }
        }).thenApply(new Function<Long, Long>() {
            @Override
            public Long apply(Long t) {
                long result = t * 5;
                System.out.println("result2=" + result);
                return result;
            }
        });

        try {
            long result = future.get();
            System.out.println(result);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * thenApply is used if you have a synchronous mapping function.
     * <p>
     * thenCompose is used if you have an asynchronous mapping function
     * <p>
     * thenCompose()是一个重要的方法允许构建健壮的和异步的管道，没有阻塞和等待的中间步骤
     */
    @Test
    public void thenCompose() throws Exception {
        CompletableFuture<Integer> future1 = CompletableFuture.supplyAsync(() -> 1)
                .thenApply(x -> x + 1);

        // It will then return a future with the result directly, rather than a nested future
        CompletableFuture<Integer> future2 = CompletableFuture.supplyAsync(() -> 1)
                .thenCompose(x -> CompletableFuture.supplyAsync(() -> x + 1));
        System.out.println(future1.get());
        System.out.println(future2.get());
    }

    /**
     * acceptEither和applyToEither和runAfterEither依赖的2个阶段中哪儿个先完成(不确定的)
     */
    @Test
    public void either() throws Exception {
        CompletableFuture<Integer> f1 = CompletableFuture.supplyAsync(new Supplier<Integer>() {
            @Override
            public Integer get() {
                try {
                    TimeUnit.SECONDS.sleep(2);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return 1;
            }
        });
        CompletableFuture<Integer> f2 = CompletableFuture.supplyAsync(new Supplier<Integer>() {
            @Override
            public Integer get() {
                try {
                    TimeUnit.SECONDS.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return 2;
            }
        });
        CompletableFuture<Integer> result = f1.applyToEither(f2, new Function<Integer, Integer>() {
            @Override
            public Integer apply(Integer t) {
                System.out.println(t);
                return t * 2;
            }
        });

        //f1和f2不同的睡眠时间(模拟任务实际耗时)会得到不同的结果
        System.out.println(result.get());
    }

    /**
     * 带有combine或者both字样的方法,依赖2个阶段都完成
     */
    @Test
    public void both() {
        // 两个CompletionStage，都完成了计算才会执行下一步的操作
        CompletableFuture<Integer> f1 = CompletableFuture.supplyAsync(new Supplier<Integer>() {
            @Override
            public Integer get() {
                try {
                    TimeUnit.SECONDS.sleep(3);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return 1;
            }
        });
        CompletableFuture<Integer> f2 = CompletableFuture.supplyAsync(new Supplier<Integer>() {
            @Override
            public Integer get() {
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return 2;
            }
        });

        // 不论f1和f2实际谁先完成, println的结果都是固定不变的; 如果是either则println的结果取决于谁先完成
        CompletableFuture<Void> resultFuture = f1.thenAcceptBoth(f2, new BiConsumer<Integer, Integer>() {
            @Override
            public void accept(Integer t, Integer u) {
                System.out.println("f1=" + t + ", f2=" + u);
            }
        });
        resultFuture.join();
    }

    /**
     * thenCombine会把两个completionStage的任务都执行完成(正常完成)后，把两个任务的结果一块交给thenCombine来处理
     * <p>
     * 区别在于thenCombine有返回值,thenAcceptBoth无返回值,runAfterBoth不需要入参也不需要返回值
     */
    @Test
    public void thenCombine() {
        CompletableFuture<Double> futurePrice = CompletableFuture.supplyAsync(() -> 100d);
        CompletableFuture<Double> futureDiscount = CompletableFuture.supplyAsync(() -> 0.8);
        CompletableFuture<Double> futureResult = futurePrice.thenCombine(futureDiscount, (price, discount) -> price * discount);
        System.out.println("最终价格为:" + futureResult.join());
    }
}

