package org.openpredict.exchange.rest.commands.util;

import lombok.extern.slf4j.Slf4j;
import org.openpredict.exchange.rest.model.internal.GatewayAssetSpec;
import org.openpredict.exchange.rest.model.internal.GatewaySymbolSpec;

import java.math.BigDecimal;

@Slf4j
public class ArithmeticHelper {

    /**
     * Convert price for one lot (order book price) from core long to BigDecimal.
     *
     * @param price      core price
     * @param symbolSpec gateway symbol spec
     * @return big decimal value
     */
    public static BigDecimal fromLongPrice(long price, GatewaySymbolSpec symbolSpec) {

        //log.debug("symbolSpec.quoteCurrency.scale={}",symbolSpec.quoteCurrency.scale);
        //log.debug("symbolSpec.stepSize={}",symbolSpec.stepSize);
        //BigDecimal res = BigDecimal.valueOf(price).scaleByPowerOfTen(symbolSpec.quoteCurrency.scale).multiply(symbolSpec.stepSize);
        BigDecimal res = BigDecimal.valueOf(price).scaleByPowerOfTen(-symbolSpec.quoteCurrency.scale);
        //log.debug("res={}",res);
        return res;
    }

    public static BigDecimal toBaseUnits(BigDecimal price, GatewayAssetSpec assetSpec) {
        return price.scaleByPowerOfTen(assetSpec.scale);
    }

    /**
     * Check if BigDecimal is integer value
     *
     * @param value - value to examine
     * @return tre if it is integer
     */
    public static boolean isIntegerValue(BigDecimal value) {
        //log.debug("value={} scale()={}", value, value.stripTrailingZeros().scale());
        return value.stripTrailingZeros().scale() <= 0;
    }
}
