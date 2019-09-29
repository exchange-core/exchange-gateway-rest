/*
 * Copyright 2019 Maksim Zheravin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package exchange.core2.rest.commands.util;

import lombok.extern.slf4j.Slf4j;
import exchange.core2.rest.model.internal.GatewayAssetSpec;
import exchange.core2.rest.model.internal.GatewaySymbolSpec;

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

    public static BigDecimal fromLongPrice(long price, GatewayAssetSpec assetSpecSpec) {
        return BigDecimal.valueOf(price).scaleByPowerOfTen(-assetSpecSpec.scale);
    }

    public static BigDecimal toBaseUnits(BigDecimal price, GatewayAssetSpec assetSpec) {
        return price.scaleByPowerOfTen(assetSpec.scale);
    }

    /**
     * Check if BigDecimal is integer value and >=0
     *
     * @param value - value to examine
     * @return tre if it is integer and not negative
     */
    public static boolean isIntegerNotNegativeValue(BigDecimal value) {
        //log.debug("value={} scale()={}", value, value.stripTrailingZeros().scale());
        return value.compareTo(BigDecimal.ZERO) >= 0 && value.stripTrailingZeros().scale() <= 0;
    }

    /**
     * Check if BigDecimal is integer value and >0
     *
     * @param value - value to examine
     * @return tre if it is integer and positive
     */
    public static boolean isIntegerPositiveNotZeroValue(BigDecimal value) {
        //log.debug("value={} scale()={}", value, value.stripTrailingZeros().scale());
        return value.compareTo(BigDecimal.ZERO) > 0 && value.stripTrailingZeros().scale() <= 0;
    }

    public static boolean isZero(BigDecimal value) {
        return value.compareTo(BigDecimal.ZERO) == 0;
    }
}
