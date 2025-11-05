package com.infosys.smartshelfx_backend.model;

/**
 * Centralised role constants to keep backend and frontend in sync.
 */
public enum Role {
    ADMIN("Admin"),
    MANAGER("Manager"),
    VENDOR("Vendor");

    private final String label;

    Role(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }
}
