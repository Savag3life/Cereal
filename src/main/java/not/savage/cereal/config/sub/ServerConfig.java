package not.savage.cereal.config.sub;

import lombok.Getter;
import lombok.NonNull;

import java.util.List;

/**
 * TODO: This class needs to be reworked & properly implemented or removed.
 * @param registeredDatasources Self updating list of pre-registered datasources. DOES NOT WORK.
 */
public record ServerConfig(
        @Getter @NonNull List<String> registeredDatasources
) {

    public boolean containsDatasource(String datasource) {
        return containsDatasourceIgnoreDistinct(datasource);
    }

    public boolean isDatasourceDistinct(String datasource) {

        if (datasource.contains(":")) {
            datasource = datasource.split(":")[0];
        }

        for (String registeredDatasource : registeredDatasources) {
            if (registeredDatasource.startsWith(datasource)) {
                return registeredDatasource.contains(":");
            }
        }

        return false;
    }

    private boolean containsDatasourceIgnoreDistinct(String datasource) {
        for (String registeredDatasource : registeredDatasources) {
            if (registeredDatasource.contains(":")) {
                registeredDatasource = registeredDatasource.split(":")[0];
            }
            if (registeredDatasource.equalsIgnoreCase(datasource)) {
                return true;
            }
        }
        return false;
    }
}
