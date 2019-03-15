package fmg.core.mosaic;

import static org.junit.Assert.assertEquals;

import java.beans.PropertyChangeEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.junit.*;

import fmg.common.LoggerSimple;
import fmg.common.Pair;
import fmg.common.notyfier.INotifyPropertyChanged;
import fmg.common.notyfier.Signal;
import fmg.common.ui.Factory;
import fmg.core.types.EMosaic;
import fmg.core.types.ESkillLevel;
import io.reactivex.Flowable;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

public class MosaicInitDataTest {


    public static void StaticInitializer() {
//        ExecutorService scheduler = Executors.newScheduledThreadPool(1);
//        Factory.DEFERR_INVOKER = scheduler::execute;

        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        Factory.DEFERR_INVOKER = run -> scheduler.schedule(run, 10, TimeUnit.MILLISECONDS);
    }

    @BeforeClass
    public static void setup() {
        LoggerSimple.put(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        LoggerSimple.put("> MosaicInitDataTest::setup");

        StaticInitializer();

        Flowable.just("UI factory inited...").subscribe(LoggerSimple::put);
    }

    @Before
    public void before() {
        LoggerSimple.put("======================================================");
    }

    @AfterClass
    public static void after() {
        LoggerSimple.put("======================================================");
        LoggerSimple.put("< MosaicInitDataTest closed");
        LoggerSimple.put("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
    }

    private static MosaicInitData createMosaicInitData() {
        MosaicInitData initData = new MosaicInitData();
        assertEquals(MosaicInitData.DEFAULT_MOSAIC_TYPE , initData.getMosaicType());
        assertEquals(MosaicInitData.DEFAULT_SIZE_FIELD_M, initData.getSizeField().m);
        assertEquals(MosaicInitData.DEFAULT_SIZE_FIELD_N, initData.getSizeField().n);
        assertEquals(MosaicInitData.DEFAULT_MINES_COUNT , initData.getMinesCount());
        assertEquals(MosaicInitData.DEFAULT_SKILL_LEVEL , initData.getSkillLevel());
        return initData;
    }

    @Test
    public void checkTheImpossibilitySetCustomSkillLevelTest() {
        LoggerSimple.put("> checkTheImpossibilitySetCustomSkillLevelTest");
        try (MosaicInitData initData = createMosaicInitData()) {
            initData.setSkillLevel(ESkillLevel.eCustom);
            Assert.fail();
        } catch (Exception ex) {
            assertEquals(IllegalArgumentException.class, ex.getClass());
        }
    }

    /**
     * Simple UnitTets wrapper for testing {@link INotifyPropertyChanged} objects
     *  @param <T> the tested object
     */
    static class NotifyPropertyChangedUnitTest<T extends INotifyPropertyChanged> implements AutoCloseable {
        private final T data;
        public Map<String /* property name */, Pair<Integer /* count of modifies */, Object /* last modified value */>> modifiedProperties = new HashMap<>();
        private final Subject<PropertyChangeEvent> subject = PublishSubject.create();
        private final Signal signal = new Signal();
        private Disposable dis;

        NotifyPropertyChangedUnitTest(T data) {
            this.data = data;
            data.addListener(this::onDataPropertyChanged);
        }

        /**
         * unit test executor
         * @param notificationsTimeoutMs timeout call validator if you do not receive a notification
         * @param maxWaitTimeoutMs maximum timeout to wait for all notifications
         * @param modificator data modifier (executable in UI thread)
         * @param validator data validator (executable in current thread)
         */
        public void run(long notificationsTimeoutMs/* = 100*/, long maxWaitTimeoutMs/* = 1000*/, Runnable modificator, Runnable validator) {
            dis = subject.timeout(notificationsTimeoutMs, TimeUnit.MILLISECONDS)
                    .subscribe(ev -> {
                        LoggerSimple.put("onNext: ev=" + ev);
                    }, ex -> {
                      //LoggerSimple.put("onError: " + ex);
                        LoggerSimple.put("timeout after " + notificationsTimeoutMs + "ms.");
                        signal.set();
                    });
            Factory.DEFERR_INVOKER.accept(modificator);
            if (!signal.await(maxWaitTimeoutMs))
                throw new RuntimeException("Wait timeout " + maxWaitTimeoutMs + "ms.");
            validator.run();
        }

        private void onDataPropertyChanged(PropertyChangeEvent ev) {
            String name = ev.getPropertyName();
            LoggerSimple.put("NotifyPropertyChangedUnitTest: onDataPropertyChanged: ev.name=" + name);
            modifiedProperties.put(name, new Pair<>(1 + (modifiedProperties.containsKey(name) ? modifiedProperties.get(name).first : 0), ev.getNewValue()));
            subject.onNext(ev);
        }

        @Override
        public void close() {
            data.removeListener(this::onDataPropertyChanged);
            if (dis != null)
                dis.dispose();
        }

    }

    @Test
    public void checkIfMosaicTypeIsChangedThenMinesCountWillAlsoBeChangedTest() {
        LoggerSimple.put("> checkIfMosaicTypeIsChangedThenMinesCountWillAlsoBeChangedTest");
        try (MosaicInitData initData = createMosaicInitData();
             NotifyPropertyChangedUnitTest<?> ut = new NotifyPropertyChangedUnitTest<>(initData))
        {
            ut.run(100, 1000,
                () -> {
                    initData.setMosaicType(EMosaic.eMosaicRhombus1);
                }, () -> {
                    LoggerSimple.put("  propertyChangedTest: checking...");
                    Assert.assertTrue(ut.modifiedProperties.containsKey(MosaicInitData.PROPERTY_MOSAIC_TYPE));
                    Assert.assertEquals(1, ut.modifiedProperties.get(MosaicInitData.PROPERTY_MOSAIC_TYPE).first.intValue());
                    Assert.assertTrue(ut.modifiedProperties.containsKey(MosaicInitData.PROPERTY_MINES_COUNT));
                    Assert.assertEquals(1, ut.modifiedProperties.get(MosaicInitData.PROPERTY_MINES_COUNT).first.intValue());
                    Assert.assertEquals(2, ut.modifiedProperties.size()); // only MosaicInitData.PROPERTY_MINES_COUNT and MosaicInitData.PROPERTY_MOSAIC_TYPE
                    Assert.assertEquals(EMosaic.eMosaicRhombus1, initData.getMosaicType());
                    Assert.assertEquals(initData.getSkillLevel().getNumberMines(EMosaic.eMosaicRhombus1), initData.getMinesCount());
                });
        }
    }

    @Test
    public void checkNoRepeatNotificationsTest() {
        LoggerSimple.put("> checkNoRepeatNotificationsTest");
        try (MosaicInitData initData = createMosaicInitData();
             NotifyPropertyChangedUnitTest<?> ut = new NotifyPropertyChangedUnitTest<>(initData))
        {
            ut.run(100, 1000,
                () -> {
                    LoggerSimple.put("    initData.minesCount={0}", initData.getMinesCount());
                    initData.setMosaicType(EMosaic.eMosaicRhombus1);
                    LoggerSimple.put("    initData.minesCount={0}", initData.getMinesCount());
                    initData.setMosaicType(EMosaic.eMosaicHexagon1);
                    LoggerSimple.put("    initData.minesCount={0}", initData.getMinesCount());
                }, () -> {
                    LoggerSimple.put("  propertyChangedTest: checking...");
                    Assert.assertTrue(ut.modifiedProperties.containsKey(MosaicInitData.PROPERTY_MOSAIC_TYPE));
                    Assert.assertEquals(1, ut.modifiedProperties.get(MosaicInitData.PROPERTY_MOSAIC_TYPE).first.intValue());
                    Assert.assertTrue(!ut.modifiedProperties.containsKey(MosaicInitData.PROPERTY_MINES_COUNT));
                    Assert.assertEquals(1, ut.modifiedProperties.size()); // one MosaicInitData.PROPERTY_MOSAIC_TYPE
                    Assert.assertEquals(EMosaic.eMosaicHexagon1, initData.getMosaicType());
                    Assert.assertEquals(initData.getSkillLevel().getNumberMines(EMosaic.eMosaicHexagon1), initData.getMinesCount());
                });
        }
    }

}
