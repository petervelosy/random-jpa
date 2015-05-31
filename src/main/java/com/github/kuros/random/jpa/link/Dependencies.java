package com.github.kuros.random.jpa.link;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Kumar Rohit on 5/10/15.
 */
public final class Dependencies {

    private List<Link> links;

    private Dependencies() {
        this.links = new ArrayList<Link>();
    }

    public static Dependencies newInstance() {
        return new Dependencies();
    }

    public Dependencies withLink(final Link link) {
        this.links.add(link);
        return this;
    }

    public Dependencies withLink(final List<Link> allLinks) {
        this.links.addAll(allLinks);
        return this;
    }

    public List<Link> getLinks() {
        return links;
    }
}