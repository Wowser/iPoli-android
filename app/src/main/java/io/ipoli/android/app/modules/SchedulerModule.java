package io.ipoli.android.app.modules;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import io.ipoli.android.quest.persistence.QuestPersistenceService;
import io.ipoli.android.quest.persistence.RepeatingQuestPersistenceService;
import io.ipoli.android.quest.schedulers.PersistentRepeatingQuestScheduler;
import io.ipoli.android.quest.schedulers.RepeatingQuestScheduler;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 6/8/16.
 */
@Module
public class SchedulerModule {

    @Provides
    @Singleton
    public RepeatingQuestScheduler provideRepeatingQuestScheduler() {
        return new RepeatingQuestScheduler();
    }

    @Provides
    @Singleton
    public PersistentRepeatingQuestScheduler providePersistentRepeatingQuestScheduler(RepeatingQuestScheduler repeatingQuestScheduler, QuestPersistenceService questPersistenceService, RepeatingQuestPersistenceService repeatingQuestPersistenceService) {
        return new PersistentRepeatingQuestScheduler(repeatingQuestScheduler, questPersistenceService, repeatingQuestPersistenceService);
    }
}
