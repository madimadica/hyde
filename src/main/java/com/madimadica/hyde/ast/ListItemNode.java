package com.madimadica.hyde.ast;

public final class ListItemNode extends ContainerBlockNode {
    private ListData listData; // owned by ListNode

    public ListItemNode(ListData listData) {
        this.listData = listData;
    }

    public ListData getListData() {
        return listData;
    }
}
