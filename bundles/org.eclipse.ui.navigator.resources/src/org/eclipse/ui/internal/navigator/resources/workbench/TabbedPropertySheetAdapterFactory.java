/*******************************************************************************
 * Copyright (c) 2006, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.navigator.resources.workbench;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.ui.navigator.CommonNavigator;
import org.eclipse.ui.navigator.resources.ProjectExplorer;
import org.eclipse.ui.views.properties.IPropertySheetPage;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;

/**
 * An property sheet page adapter factory for the Project Explorer.
 *
 * @since 3.2
 */
public class TabbedPropertySheetAdapterFactory
    implements IAdapterFactory {

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getAdapter(Object adaptableObject, Class<T> adapterType) {
        if (adaptableObject instanceof ProjectExplorer) {
        	if (IPropertySheetPage.class == adapterType)
				return (T) new TabbedPropertySheetPage(
                    new TabbedPropertySheetProjectExplorerContributor(
                        (CommonNavigator) adaptableObject));
        }
        return null;
    }

    @Override
	public Class<?>[] getAdapterList() {
        return new Class[] {IPropertySheetPage.class};
    }

}
