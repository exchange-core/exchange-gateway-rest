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
package exchange.core2.rest.commands.admin;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public final class RestApiAdminAsset {

    public final String assetCode;
    public final int assetId;
    public final int scale; // asset scale - 2 for most currencies, 8 for BTC, 0 for JPY, etc

    @JsonCreator
    public RestApiAdminAsset(
            @JsonProperty("assetCode") String assetCode,
            @JsonProperty("assetId") int assetId,
            @JsonProperty("scale") int scale) {

        this.assetCode = assetCode;
        this.assetId = assetId;
        this.scale = scale;
    }

    @Override
    public String toString() {
        return "[ASSET " + assetCode + " " + assetId + "]";
    }
}
