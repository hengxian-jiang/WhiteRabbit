package com.arcadia.whiteRabbitService.util;

import org.ohdsi.databases.RichConnection;
import org.ohdsi.whiteRabbit.DbSettings;

public class RichConnectionUtil {
    public static RichConnection createRichConnection(DbSettings dbSettings) {
        return new RichConnection(
                dbSettings.server,
                dbSettings.domain,
                dbSettings.user,
                dbSettings.password,
                dbSettings.dbType
        );
    }
}
