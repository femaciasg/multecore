package no.hvl.multecore.core.events;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.jobs.Job;

import no.hvl.multecore.common.Debugger;
import no.hvl.multecore.core.Constants;


public class Utils {
	
	// Get all relevant resources within a container (normally a project)
	public static Map<String,List<IResource>> getAllResources(IContainer container) {
		IResource[] containerResourcesArray = null;
		try {
			containerResourcesArray = container.members(false);
		} catch (CoreException e) {
			Debugger.logError("Could not retrieve resource \"" + container.getName() + "\" contents");
			return new HashMap<String,List<IResource>>();
		}
		Map<String,List<IResource>> containerResources = new HashMap<String,List<IResource>>();
		for (int i=0;i<containerResourcesArray.length;i++) {
			IResource ir = containerResourcesArray[i];
			if (ir instanceof IContainer) {
				Map<String, List<IResource>> subcontainerResources = getAllResources((IContainer) ir);
				for (String extension : Constants.MLM_RELEVANT_EXTENSIONS_LIST) {
					containerResources.get(extension).addAll(subcontainerResources.get(extension));
				}
			} else {
				for (String extension : Constants.MLM_RELEVANT_EXTENSIONS_LIST) {
					containerResources.computeIfAbsent(extension, k -> new ArrayList<IResource>());
				}
				String extension = ir.getFileExtension().toLowerCase();
				if (Constants.MLM_RELEVANT_EXTENSIONS_LIST.contains(extension))
					containerResources.get(extension).add(ir);
			}
		}
		return containerResources;
	}
	
	
	// Schedules a job
	public static void scheduleJob(Job job) {
		job.setPriority(Job.LONG);
		job.setUser(true);
		job.schedule();
	}
	
}
