package net.furizon.zebra_proxy.utils;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

public class TimeConst {
    public static final OffsetDateTime MIN_ODT = OffsetDateTime.ofInstant(Instant.EPOCH, ZoneId.systemDefault());
    //Not a problem for me :D
    public static final OffsetDateTime MAX_ODT = OffsetDateTime.of(2300, 2, 2, 2, 2, 2, 2, ZoneOffset.UTC);

}
