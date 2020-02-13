package hgu.csee.isel.alinew.szz;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;

import hgu.csee.isel.alinew.szz.exception.EmptyHunkTypeException;
import hgu.csee.isel.alinew.szz.graph.AnnotationGraphBuilder;
import hgu.csee.isel.alinew.szz.graph.AnnotationGraphModel;
import hgu.csee.isel.alinew.szz.model.Line;
import hgu.csee.isel.alinew.szz.model.RevsWithPath;
import hgu.csee.isel.alinew.szz.trace.Tracer;
import hgu.csee.isel.alinew.szz.util.GitUtils;

public class AgSZZ {
	private final String GIT_DIR = "/Users/kimseokjin/git/incubator-iotdb";
	private final String FIX_COMMIT = "17f3a429a5c9a243abb61b078d9511c523c3954e";
	private List<String> BFCList = new ArrayList<>();
	
	private static Git git;
	private Repository repo;
	
	
	public static void main(String[] args) {
		new AgSZZ().run();	
	}
	
	private void run() {
		try {
			git = Git.open(new File(GIT_DIR));
			repo = git.getRepository();
			List<RevCommit> revs = GitUtils.getRevs(git);
			BFCList.add(FIX_COMMIT);
			
			RevsWithPath revsWithPath = GitUtils.collectRevsWithSpecificPath(GitUtils.configurePathRevisionList(repo, revs));
			
			// Phase 1 : Build the annotation graph
			AnnotationGraphBuilder agb = new AnnotationGraphBuilder();
			AnnotationGraphModel agm = agb.buildAnnotationGraph(repo, revsWithPath);
			
			// TEST
//			Iterator<RevCommit> iter = agm.keySet().iterator();
//			
//			int revCnt = 1;
//			while(iter.hasNext()) {
//				
//				RevCommit rev = iter.next();
//				
//				System.out.println("\nRev Count : " + revCnt + "=========================\n");
//				
//				System.out.println("rev : " + rev.getName());
//				HashMap<String, ArrayList<Line>> map = agm.get(rev);
//				Iterator<String> iter2 = map.keySet().iterator();
//				
//				while(iter2.hasNext()) {
//					String path = iter2.next();
//					System.out.println("path : " + path);
//					ArrayList<Line> lines = map.get(path);
//					
//					for(Line l : lines) {
//						System.out.println("\trev : " + l.getRev());
//						System.out.println("\tpath : " + l.getPath() + "\n\n");
//						
//						System.out.println("content : " + l.getContent());
//						System.out.println("index : " + l.getIdx());
//						System.out.println("type : " + l.getLineType() + "\n") ;
//					}
//					
//				}
//				
//				revCnt++;
//			}
			
			// Phase 2 : Trace and collect BIC candidates
			// Phase 3 : Filter out format changes, comments, etc among BIC candidates
			Tracer tracer = new Tracer();
			List<Line> BILines = tracer.collectBILines(repo, revs, agm, revsWithPath, BFCList);
			
			//TEST
			System.out.println("size: " + BILines.size());
			for(Line line : BILines) {
				System.out.println("BIC: "+line.getIdx());
				System.out.println("Path: "+line.getPath());
				System.out.println("Revision: "+line.getRev());
				System.out.println("Content: "+line.getContent() +"\n");
			}	
			
		} catch (IOException | GitAPIException | EmptyHunkTypeException e) {
			e.printStackTrace();
		}
	}
}
