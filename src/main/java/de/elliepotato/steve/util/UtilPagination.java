package de.elliepotato.steve.util;

public class UtilPagination {

    public static int getPageCount(int elements, int maxPerPage) {
        return (int) Math.round(Math.ceil((double) elements / maxPerPage));
    }

    public static int getPageElementIndex(int currentPage, int totalPages, int maxElementsPerPage) {
        int startIndex = 0;
        if (currentPage >= 0 && currentPage < totalPages) {
            startIndex = currentPage * maxElementsPerPage;
        }
        return startIndex;
    }

}
