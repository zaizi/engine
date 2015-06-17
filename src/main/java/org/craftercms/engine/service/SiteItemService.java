/*
 * Copyright (C) 2007-2013 Crafter Software Corporation.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.craftercms.engine.service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.craftercms.core.service.ContentStoreService;
import org.craftercms.core.service.Item;
import org.craftercms.core.service.ItemFilter;
import org.craftercms.core.service.Tree;
import org.craftercms.core.service.impl.CompositeItemFilter;
import org.craftercms.engine.model.SiteItem;
import org.craftercms.engine.model.converters.ModelValueConverter;
import org.craftercms.engine.service.context.SiteContext;
import org.craftercms.engine.service.filter.ExcludeByNameItemFilter;
import org.craftercms.engine.service.filter.ExpectedNodeValueItemFilter;
import org.craftercms.engine.service.filter.IncludeByNameItemFilter;
import org.springframework.beans.factory.annotation.Required;

/**
 * Service for accessing {@link org.craftercms.engine.model.SiteItem}s.
 *
 * @author Alfonso Vásquez
 */
public class SiteItemService {

    protected ContentStoreService storeService;
    protected Map<String, ModelValueConverter<?>> modelValueConverters;
    protected List<ItemFilter> defaultFilters;
    protected Comparator<SiteItem> sortComparator;

    @Required
    public void setStoreService(ContentStoreService storeService) {
        this.storeService = storeService;
    }

    @Required
    public void setModelValueConverters(Map<String, ModelValueConverter<?>> modelValueConverters) {
        this.modelValueConverters = modelValueConverters;
    }

    public void setDefaultFilters(List<ItemFilter> defaultFilters) {
        this.defaultFilters = defaultFilters;
    }

    public void setSortComparator(Comparator<SiteItem> sortComparator) {
        this.sortComparator = sortComparator;
    }

    public SiteItem getSiteItem(String url) {
        SiteContext siteContext = SiteContext.getCurrent();
        Item item = storeService.findItem(siteContext.getContext(), url);

        if (item != null) {
            return new SiteItem(item, modelValueConverters);
        } else {
            return null;
        }
    }

    public SiteItem getSiteTree(String url, int depth)  {
        return getSiteTree(url, depth, null, null, null);
    }

    public SiteItem getSiteTree(String url, int depth, String includeByNameRegex, String excludeByNameRegex) {
        return getSiteTree(url, depth, includeByNameRegex, excludeByNameRegex, null);
    }

    public SiteItem getSiteTree(String url, int depth, String includeByNameRegex, String excludeByNameRegex,
                                String[]... nodeXPathAndExpectedValuePairs) {
        CompositeItemFilter compositeFilter = new CompositeItemFilter();

        if (CollectionUtils.isNotEmpty(defaultFilters)) {
            for (ItemFilter defaultFilter : defaultFilters) {
                compositeFilter.addFilter(defaultFilter);
            }
        }

        if (StringUtils.isNotEmpty(includeByNameRegex)) {
            compositeFilter.addFilter(new IncludeByNameItemFilter(includeByNameRegex));
        }
        if (StringUtils.isNotEmpty(excludeByNameRegex)) {
            compositeFilter.addFilter(new ExcludeByNameItemFilter(excludeByNameRegex));
        }

        if (ArrayUtils.isNotEmpty(nodeXPathAndExpectedValuePairs)) {
            for (String[] nodeXPathAndExpectedValuePair : nodeXPathAndExpectedValuePairs) {
                String nodeXPathQuery = nodeXPathAndExpectedValuePair[0];
                String expectedNodeValueRegex = nodeXPathAndExpectedValuePair[1];

                compositeFilter.addFilter(new ExpectedNodeValueItemFilter(nodeXPathQuery, expectedNodeValueRegex));
            }
        }

        SiteContext siteContext = SiteContext.getCurrent();
        Tree tree = storeService.findTree(siteContext.getContext(), null, url, depth, compositeFilter, null);

        if (tree != null) {
            return new SiteItem(tree, modelValueConverters, sortComparator);
        } else {
            return null;
        }
    }

}
