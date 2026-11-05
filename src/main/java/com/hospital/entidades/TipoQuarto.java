package com.hospital.entidades;

/**
 * Tipos de quarto do hospital. Cada tipo define a faixa de leitos permitida e sabe criar a
 * subclasse de {@link Quarto} correspondente (fábrica usada pela camada de serviço).
 */
public enum TipoQuarto {

    ENFERMARIA("Enfermaria", 2, 8) {
        @Override
        public Quarto novoQuarto() {
            return new Enfermaria();
        }
    },
    APARTAMENTO("Apartamento", 1, 2) {
        @Override
        public Quarto novoQuarto() {
            return new Apartamento();
        }
    },
    UTI("Unidade de Terapia Intensiva", 1, 10) {
        @Override
        public Quarto novoQuarto() {
            return new UnidadeTerapiaIntensiva();
        }
    },
    ISOLAMENTO("Quarto de isolamento", 1, 1) {
        @Override
        public Quarto novoQuarto() {
            return new QuartoIsolamento();
        }
    };

    private final String descricao;
    private final int capacidadeMinima;
    private final int capacidadeMaxima;

    TipoQuarto(String descricao, int capacidadeMinima, int capacidadeMaxima) {
        this.descricao = descricao;
        this.capacidadeMinima = capacidadeMinima;
        this.capacidadeMaxima = capacidadeMaxima;
    }

    /** Cria uma nova instância (ainda sem dados) do tipo de quarto. */
    public abstract Quarto novoQuarto();

    public String getDescricao() {
        return descricao;
    }

    public int getCapacidadeMinima() {
        return capacidadeMinima;
    }

    public int getCapacidadeMaxima() {
        return capacidadeMaxima;
    }
}
