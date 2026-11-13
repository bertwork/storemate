package model;

import java.math.BigDecimal;

public record Product(int id, String name, String category, BigDecimal price, String imagePath) {}