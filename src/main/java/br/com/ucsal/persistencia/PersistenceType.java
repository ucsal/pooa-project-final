package br.com.ucsal.persistencia;

public enum PersistenceType {
    MEMORIA("MemoriaProdutoRepository"),
    HSQL("HSQLProdutoRepository");

    private final String className;

    PersistenceType(String className) {
        this.className = className;
    }

    public String getClassName() {
        return className;
    }
}
