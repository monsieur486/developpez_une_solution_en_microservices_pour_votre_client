package com.mr486.mswebclient.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Page de résultats renvoyée par les API : le contenu et les informations de
 * navigation.
 *
 * <p><b>Exemple :</b> une page 0 de 20 patients sur 45 porte totalPages=3 et
 * totalElements=45 ; aPlusieursPages() vaut alors true.</p>
 *
 * @param <T> type des éléments de la page
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PageReponse<T> {

    private List<T> content;
    private int page;
    private int totalPages;
    private long totalElements;

    /**
     * Indique si la navigation de pages mérite d'être affichée.
     *
     * <p><b>Exemple :</b> avec totalPages=3, aPlusieursPages() vaut true ; avec
     * une seule page, false.</p>
     *
     * @return true si le résultat compte plus d'une page
     */
    public boolean aPlusieursPages() {
        return totalPages > 1;
    }
}
