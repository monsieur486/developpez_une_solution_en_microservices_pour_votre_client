package com.mr486.msnotes.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

/**
 * Page de résultats exposée par l'API : le contenu et les informations de
 * navigation, sans détail d'implémentation Spring Data.
 *
 * <p><b>Exemple :</b> une page 0 de 5 notes sur 12 porte totalPages=3 et
 * totalElements=12.</p>
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
     * Construit la réponse à partir d'une page Spring Data.
     *
     * <p><b>Exemple :</b> depuis(page) recopie le contenu, le numéro de page et
     * les totaux de la page fournie.</p>
     *
     * @param <T>  type des éléments de la page
     * @param page la page Spring Data à exposer
     * @return la réponse de page correspondante
     */
    public static <T> PageReponse<T> depuis(Page<T> page) {
        return PageReponse.<T>builder()
                .content(page.getContent())
                .page(page.getNumber())
                .totalPages(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .build();
    }
}
