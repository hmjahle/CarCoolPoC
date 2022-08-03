package com.visma.of.cps.routeEvaluator.solver.algorithm;

import java.util.Enumeration;

/**
 * The interface is used to find nodes to extend a label to this is returned as a part of the ExtendToInfo along with
 * the node set the node belongs to.
 */
interface IExtendInfo {

    /**
     * Return an enumeration of the potential ways to extend the label.
     *
     * @param label Label to extend.
     * @return Enumeration.
     */
    Enumeration<ExtendToInfo> extend(Label label);

    /**
     * Must create and return an empty IResource that is the same as the
     * implementation of the IExtendInfo use when extending labels.
     *
     * @return IResource.
     */
    IResource createEmptyResource();
}
