package io.github.projectunified.craftitem.core;

import java.util.UUID;

public interface Item {
    void setName(String name);

    void setAmount(int amount);

    UUID getOwner();
}
