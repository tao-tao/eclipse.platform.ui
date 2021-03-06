/*******************************************************************************
 * Copyright (c) 2005, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Andrey Loskutov <loskutov@gmx.de> - generified interface, bug 461762
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 478686
 *******************************************************************************/
package org.eclipse.ui.ide;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.mapping.ResourceMapping;
import org.eclipse.core.resources.mapping.ResourceTraversal;
import org.eclipse.core.runtime.Adapters;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.statushandlers.StatusManager;

/**
 * Utility class for manipulating resources and determining correspondences
 * between resources and workbench objects.
 * <p>
 * This class provides all its functionality via static methods.
 * It is not intended to be instantiated or subclassed.
 * </p>
 *
 * @since 3.1
 */
public final class ResourceUtil {

    private ResourceUtil() {
        // prevent instantiation
    }

    /**
     * Returns the file corresponding to the given editor input, or <code>null</code>
     * if there is no applicable file.
     * Returns <code>null</code> if the given editor input is <code>null</code>.
     *
     * @param editorInput the editor input, or <code>null</code>
     * @return the file corresponding to the editor input, or <code>null</code>
     */
    public static IFile getFile(IEditorInput editorInput) {
        // Note: do not treat IFileEditorInput as a special case.  Use the adapter mechanism instead.
        // See Bug 87288 [IDE] [EditorMgmt] Should avoid explicit checks for [I]FileEditorInput
		return Adapters.adapt(editorInput, IFile.class);
    }

    /**
     * Returns the resource corresponding to the given editor input, or <code>null</code>
     * if there is no applicable resource.
     * Returns <code>null</code> if the given editor input is <code>null</code>.
     *
     * @param editorInput the editor input
     * @return the file corresponding to the editor input, or <code>null</code>
     */
    public static IResource getResource(IEditorInput editorInput) {
		if (editorInput == null) {
			return null;
		}
        // Note: do not treat IFileEditorInput as a special case.  Use the adapter mechanism instead.
        // See Bug 87288 [IDE] [EditorMgmt] Should avoid explicit checks for [I]FileEditorInput
		IResource resource = Adapters.adapt(editorInput, IResource.class);
		if (resource != null) {
			return resource;
		}
        // the input may adapt to IFile but not IResource
		return Adapters.adapt(editorInput, IFile.class);
    }

    /**
     * Returns the editor in the given page whose input represents the given file,
     * or <code>null</code> if there is no such editor.
     *
     * @param page the workbench page
     * @param file the file
     * @return the matching editor, or <code>null</code>
     */
    public static IEditorPart findEditor(IWorkbenchPage page, IFile file) {
        // handle the common case where the editor input is a FileEditorInput
        IEditorPart editor = page.findEditor(new FileEditorInput(file));
        if (editor != null) {
            return editor;
        }
        // check for editors that have their own kind of input that adapts to IFile,
        // being careful not to force loading of the editor
		for (IEditorReference ref : page.getEditorReferences()) {
            IEditorPart part = ref.getEditor(false);
            if (part != null) {
                IFile editorFile = getFile(part.getEditorInput());
                if (editorFile != null && file.equals(editorFile)) {
                    return part;
                }
            }
        }
        return null;
    }

    /**
     * Returns the resource corresponding to the given model element, or <code>null</code>
     * if there is no applicable resource.
     *
     * @param element the model element, or <code>null</code>
     * @return the resource corresponding to the model element, or <code>null</code>
     * @since 3.2
     */
    public static IResource getResource(Object element) {
		return Adapters.adapt(element, IResource.class);
    }

    /**
     * Returns the file corresponding to the given model element, or <code>null</code>
     * if there is no applicable file.
     *
     * @param element the model element, or <code>null</code>
     * @return the resource corresponding to the model element, or <code>null</code>
     * @since 3.2
     */
    public static IFile getFile(Object element) {
		if (element == null) {
			return null;
		}

		// try direct instanceof check
		if (element instanceof IFile) {
			return (IFile) element;
		}

		// try for ResourceMapping
		ResourceMapping mapping = getResourceMapping(element);
		if (mapping != null) {
			return getFileFromResourceMapping(mapping);
		}

		// try for IFile adapter (before IResource adapter, since it's more specific)
		IFile file = Adapters.adapt(element, IFile.class);
		if (file != null) {
			return file;
		}

		// try for IResource adapter
		IResource resource = Adapters.adapt(element, IResource.class);
		if (resource instanceof IFile) {
			return (IFile) resource;
		}
		return null;
    }

	/**
     * Returns the resource mapping corresponding to the given model element, or <code>null</code>
     * if there is no applicable resource mapping.
     *
     * @param element the model element, or <code>null</code>
     * @return the resource mapping corresponding to the model element, or <code>null</code>
     * @since 3.2
     */
    public static ResourceMapping getResourceMapping(Object element) {
		return Adapters.adapt(element, ResourceMapping.class);
	}

    /**
     * Tries to extra a single file from the given resource mapping.
     * Returns the file if the mapping maps to a single file, or <code>null</code>
     * if it maps to zero or multiple files.
     *
     * @param mapping the resource mapping
     * @return the file, or <code>null</code>
     */
    private static IFile getFileFromResourceMapping(ResourceMapping mapping) {
    	IResource resource = getResourceFromResourceMapping(mapping);
    	if (resource instanceof IFile) {
    		return (IFile) resource;
    	}
    	return null;
    }

    /**
     * Tries to extra a single resource from the given resource mapping.
     * Returns the resource if the mapping maps to a single resource, or <code>null</code>
     * if it maps to zero or multiple resources.
     *
     * @param mapping the resource mapping
     * @return the resource, or <code>null</code>
     */
    private static IResource getResourceFromResourceMapping(ResourceMapping mapping) {
		try {
			ResourceTraversal[] traversals = mapping.getTraversals(null, null);
	    	if (traversals.length != 1) {
	    		return null;
	    	}
	    	ResourceTraversal traversal = traversals[0];
			// TODO: need to honor traversal flags
	    	IResource[] resources = traversal.getResources();
	    	if (resources.length != 1) {
	    		return null;
	    	}
	    	return resources[0];
		} catch (CoreException e) {
			StatusManager.getManager().handle(e, IDEWorkbenchPlugin.IDE_WORKBENCH);
			return null;
		}
	}


	/**
	 * See Javadoc of {@link Adapters#adapt(Object, Class, boolean)}.
	 *
	 * @since 3.2
	 *
	 * @deprecated Use {@link Adapters#adapt(Object, Class, boolean)} instead.
	 */
	@Deprecated
	public static <T> T getAdapter(Object element, Class<T> adapterType, boolean forceLoad) {
		return Adapters.adapt(element, adapterType, forceLoad);
	}
}
