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
package exchange.core2.rest.model.api;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;

@AllArgsConstructor
@Getter
public enum TimeFrame {

    // TODO fix 5,15,4 values
    M1(Duration.ofMinutes(1), ChronoUnit.MINUTES),
    M5(Duration.ofMinutes(5), ChronoUnit.MINUTES),
    M15(Duration.ofMinutes(15), ChronoUnit.MINUTES),
    H1(Duration.ofHours(1), ChronoUnit.HOURS),
    H4(Duration.ofHours(4), ChronoUnit.HOURS),
    D1(Duration.ofDays(1), ChronoUnit.DAYS),
    W1(Duration.ofDays(7), ChronoUnit.WEEKS);

    private final Duration duration;
    private final TemporalUnit truncateUnit;
}
