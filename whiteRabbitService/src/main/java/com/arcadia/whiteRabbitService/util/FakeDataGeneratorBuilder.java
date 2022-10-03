package com.arcadia.whiteRabbitService.util;

import org.ohdsi.whiteRabbit.DbSettings;
import org.ohdsi.whiteRabbit.Interrupter;
import org.ohdsi.whiteRabbit.Logger;
import org.ohdsi.whiteRabbit.fakeDataGenerator.FakeDataGenerator;

public class FakeDataGeneratorBuilder {
    public static FakeDataGenerator createFakeDataGenerator(Logger logger, Interrupter interrupter) {
        FakeDataGenerator generator = new FakeDataGenerator();
        generator.setLogger(logger);
        generator.setInterrupter(interrupter);

        return generator;
    }
}
