package com.madimadica.hyde.ast;

public final class ListNode extends ContainerBlockNode {
    private ListData listData;

    public ListData getListData() {
        return listData;
    }

    public ListNode(ListData listData) {
        this.listData = listData;
    }

    public boolean allows(ListData listData) {
        return this.listData.isCompatibleWith(listData);
    }
}
