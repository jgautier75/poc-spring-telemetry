package com.acme.jga.rest.versioning;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class WebApiVersions {
    public static final String V1 = "v1";
    public static final String API_PREFIX = "/api";
    public static final String V1_PREFIX = "/" + V1;

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class TenantsResourceVersion {
        public static final String CATEGORY = "tenants";
        public static final String ROOT = API_PREFIX + V1_PREFIX + "/tenants";
        public static final String WITH_UID = ROOT + "/{uid}";

        @Getter
        public enum Endpoints {
            LIST_V1(CATEGORY, "list", ROOT, V1),
            CREATE(CATEGORY, "create", ROOT, V1),
            UPDATE(CATEGORY, "update", WITH_UID, V1),
            DELETE(CATEGORY, "delete", WITH_UID, V1),
            GET_DETAILS(CATEGORY, "details", WITH_UID, V1);
            String category;
            String code;
            String uri;
            String version;

            Endpoints(String category, String code, String uri, String version) {
                this.category = category;
                this.code = code;
                this.uri = uri;
                this.version = version;
            }
        }
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class OrganizationsResourceVersion {
        public static final String CATEGORY = "organizations";
        public static final String ROOT = API_PREFIX + V1_PREFIX + "/tenants/{tenantUid}/organizations";
        public static final String WITH_UID = ROOT + "/{orgUid}";

        @Getter
        public enum Endpoints {
            LIST_V1(CATEGORY, "list", ROOT, V1),
            CREATE(CATEGORY, "create", ROOT, V1),
            UPDATE(CATEGORY, "update", WITH_UID, V1),
            DELETE(CATEGORY, "delete", WITH_UID, V1),
            GET_DETAILS(CATEGORY, "details", WITH_UID, V1);
            String category;
            String code;
            String uri;
            String version;

            Endpoints(String category, String code, String uri, String version) {
                this.category = category;
                this.code = code;
                this.uri = uri;
                this.version = version;
            }
        }
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class UsersResourceVersion {
        public static final String CATEGORY = "users";
        public static final String ROOT = API_PREFIX + V1_PREFIX + "/tenants/{tenantUid}/organizations/{orgUid}/users";
        public static final String WITH_UID = ROOT + "/{userUid}";

        @Getter
        public enum Endpoints {
            LIST_V1(CATEGORY, "list", ROOT, V1),
            CREATE(CATEGORY, "create", ROOT, V1),
            UPDATE(CATEGORY, "update", WITH_UID, V1),
            DELETE(CATEGORY, "delete", WITH_UID, V1),
            GET_DETAILS(CATEGORY, "details", WITH_UID, V1);
            String category;
            String code;
            String uri;
            String version;

            Endpoints(String category, String code, String uri, String version) {
                this.category = category;
                this.code = code;
                this.uri = uri;
                this.version = version;
            }
        }
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class SectorsResourceVersion {
        public static final String CATEGORY = "sectors";
        public static final String ROOT = API_PREFIX + V1_PREFIX + "/tenants/{tenantUid}/organizations/{orgUid}/sectors";
        public static final String WITH_UID = ROOT + "/{sectorUid}";

        @Getter
        public enum Endpoints {
            LIST_V1(CATEGORY, "list", ROOT, V1),
            CREATE(CATEGORY, "create", ROOT, V1),
            UPDATE(CATEGORY, "update", WITH_UID, V1),
            DELETE(CATEGORY, "delete", WITH_UID, V1),
            GET_DETAILS(CATEGORY, "details", WITH_UID, V1);
            String category;
            String code;
            String uri;
            String version;

            Endpoints(String category, String code, String uri, String version) {
                this.category = category;
                this.code = code;
                this.uri = uri;
                this.version = version;
            }
        }
    }

    public static class SystemResourceVersion {
        public static final String CATEGORY = "system";
        public static final String ROOT = API_PREFIX + V1_PREFIX + "/" + CATEGORY;
        public static final String KAFKA_WAKEUP = ROOT + "/wakeup";
        public static final String VERSIONS = ROOT + "/versions";
        public static final String TECH_GAUGE_RESET = ROOT + "/techGaugeReset";
        public static final String ERRORS_LIST = ROOT + "/errors";
        public static final String ERRORS_READ = ERRORS_LIST + "/{fileName}";
        public static final String VAULT_STORE = ROOT + "/vault";
        public static final String VAULT_READ = VAULT_STORE;
        public static final String VAULT_LIST = VAULT_STORE + "/list";
        public static final String DEPS_LIST = ROOT + "/dependencies";

        @Getter
        public enum Endpoints {
            EVENT_WAKEUP(CATEGORY, "events_wakeup", KAFKA_WAKEUP, V1),
            VERSIONS(CATEGORY, "versions", SystemResourceVersion.VERSIONS, V1),
            TECH_GAUGE_RESET(CATEGORY, "tech_gauge_reset", SystemResourceVersion.TECH_GAUGE_RESET, V1),
            ERRORS_LIST(CATEGORY, "errors_list", SystemResourceVersion.ERRORS_LIST, V1),
            ERRORS_READ(CATEGORY, "errors_read", SystemResourceVersion.ERRORS_READ, V1),
            VAULT_STORE(CATEGORY, "vault_store", SystemResourceVersion.VAULT_STORE, V1),
            VAULT_READ(CATEGORY, "vault_read", SystemResourceVersion.VAULT_READ, V1),
            VAULT_LIST(CATEGORY, "vault_list", SystemResourceVersion.VAULT_LIST, V1),
            DEPENDENCIES_LIST(CATEGORY, "dependencies_list", SystemResourceVersion.DEPS_LIST, V1);
            String category;
            String code;
            String uri;
            String version;

            Endpoints(String category, String code, String uri, String version) {
                this.category = category;
                this.code = code;
                this.uri = uri;
                this.version = version;
            }
        }
    }

    public static class SpiResourceVersion {
        public static final String CATEGORY = "spi";
        public static final String ROOT = API_PREFIX + V1_PREFIX + "/" + CATEGORY;
        public static final String FIND_USER = ROOT + "/user";

        @Getter
        public enum Endpoints {
            USER(CATEGORY, "user_find", FIND_USER, V1);
            String category;
            String code;
            String uri;
            String version;

            Endpoints(String category, String code, String uri, String version) {
                this.category = category;
                this.code = code;
                this.uri = uri;
                this.version = version;
            }
        }
    }

}
