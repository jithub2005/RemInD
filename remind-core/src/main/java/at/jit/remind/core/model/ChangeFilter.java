package at.jit.remind.core.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import at.jit.remind.core.exception.RemindModelException;
import at.jit.remind.core.model.content.Target;
import at.jit.remind.core.xml.Change;
import at.jit.remind.core.xml.InstallationDocument;

public class ChangeFilter
{
	private Element root;
	private UserInput userInput;

	public ChangeFilter on(Element root)
	{
		this.root = root;

		return this;
	}

	public ChangeFilter with(UserInput userInput)
	{
		this.userInput = userInput;

		return this;
	}

	/**
	 * This method provides filtering and sorting algorithm for uploaded XML file. Because of algorithm complexity here is one example:<br/>
	 * <br/>
	 * 
	 * Imagine that we have next data structure, data in first row presenting Changes with revision number (same letter means same Source and Target, only
	 * revision number is different), in second line are indexes (positions) in collection
	 * <table border='1' cellpadding='3'>
	 * <tr>
	 * <th>A1</th>
	 * <th>B1</th>
	 * <th>A2</th>
	 * <th>A3</th>
	 * <th>B2</th>
	 * </tr>
	 * <tr>
	 * <td>0</td>
	 * <td>1</td>
	 * <td>2</td>
	 * <td>3</td>
	 * <td>4</td>
	 * </tr>
	 * </table>
	 * <br/>
	 * <br/>
	 * 
	 * Because ChangeModel with highest revision should be moved to the first place in collection we are expecting next data structure:
	 * <table border='1' cellpadding='3'>
	 * <tr>
	 * <th>A3</th>
	 * <th>B2</th>
	 * <th>A2</th>
	 * <th>A1</th>
	 * <th>B1</th>
	 * </tr>
	 * <tr>
	 * <td>0</td>
	 * <td>1</td>
	 * <td>2</td>
	 * <td>3</td>
	 * <td>4</td>
	 * </tr>
	 * </table>
	 * <br/>
	 * <br/>
	 * 
	 * Here is explanation of algorithm:<br/>
	 * <br/>
	 * 1. We are going through list of nodes that we get with unmarshaling root document. Than we are converting nodes to ChangeModels and if they applies to
	 * userInput we are adding this ChangeModel in changeModelList, put ChangeModel and Node in changeModelNodeMap and put redni broj and ParentNode in
	 * indexParentMap. If not, we are removing this ChangeModel from parent node.<br/>
	 * <code>List<ChangeModel> changeModelList</code> - new created list of applied ChangeModels<br/>
	 * <code>Map<ChangeModel, Node> changeModelNodeMap </code> - <br/>
	 * <code>Map<Integer, Node> indexParentMap</code> - <br/>
	 * <br/>
	 * 
	 * 2. Now we going through new created list and group almost equals elements in new list.<br/>
	 * 
	 * First we want to collect all almost the same changes in one list: <br/>
	 * <br/>
	 * Here are two lists created:<br/>
	 * <code>List<ChangeModel> almostEqualChangeModels</code> - presented in table <br/>
	 * <code>List<Integer> almostEqualChangeModelIndices</code> - indexes of same elements from changedModelList <br/>
	 * <table border='1' cellpadding='3'>
	 * <tr>
	 * <th>A1</th>
	 * <th>A2</th>
	 * <th>A3</th>
	 * </tr>
	 * <tr>
	 * <td>0</td>
	 * <td>2</td>
	 * <td>3</td>
	 * </tr>
	 * </table>
	 * <br/>
	 * <table border='1' cellpadding='3'>
	 * <tr>
	 * <th>B1</th>
	 * <th>B2</th>
	 * </tr>
	 * <tr>
	 * <td>1</td>
	 * <td>4</td>
	 * </tr>
	 * </table>
	 * <br/>
	 * <br/>
	 * 
	 * When elements are grouped we will sort new filled list by revision number.<br/>
	 * But we also want to set index, so we are creating new map with index of element from almostEqualChangeModelIndices list as key and index of changeModel
	 * element from changeModelList as value <br/>
	 * 
	 * This map tells us old position (index) and new position where we are going to move our element.<br/>
	 * <br/>
	 * 
	 * 3. Than we are going through entry set of this map. Take node from changeModelNodeMap by element from changeModelList where entry value is key. and
	 * append this node to his parent Node.<br/>
	 * 
	 * <table border='1' cellpadding='3'>
	 * <tr>
	 * <th>A3</th>
	 * <th>A2</th>
	 * <th>A1</th>
	 * </tr>
	 * <tr>
	 * <td>3</td>
	 * <td>2</td>
	 * <td>0</td>
	 * </tr>
	 * </table>
	 * <br/>
	 * <table border='1' cellpadding='3'>
	 * <tr>
	 * <th>B2</th>
	 * <th>B1</th>
	 * </tr>
	 * <tr>
	 * <td>4</td>
	 * <td>1</td>
	 * </tr>
	 * </table>
	 * <br/>
	 * <br/>
	 * 
	 * @return
	 * @throws RemindModelException
	 */
	public InstallationDocument apply() throws RemindModelException
	{
		try
		{
			JAXBContext context = JAXBContext.newInstance(Change.class);
			Unmarshaller unmarshaller = context.createUnmarshaller();

			NodeList changeList = root.getElementsByTagName(RemindModelBase.getTagName(Change.class));
			List<ChangeModel> changeModelList = new ArrayList<ChangeModel>();
			Map<ChangeModel, Node> changeModelNodeMap = new HashMap<ChangeModel, Node>();
			Map<Integer, Node> indexParentMap = new HashMap<Integer, Node>();

			int cnt = 0;
			for (int i = 0; i < changeList.getLength(); ++i)
			{
				Node node = changeList.item(i);

				Change change = unmarshaller.unmarshal(node, Change.class).getValue();
				ChangeModel changeModel = new ChangeModel();
				changeModel.update(change);

				if (changeModel.appliesFor(userInput))
				{
					changeModelList.add(changeModel);
					changeModelNodeMap.put(changeModel, node);
					indexParentMap.put(cnt++, node.getParentNode());
				}
				else
				{
					node.getParentNode().removeChild(node);
					--i;
				}
			}

			Map<Integer, Integer> orderMap = new HashMap<Integer, Integer>();
			Set<ChangeModel> processedChangeModels = new HashSet<ChangeModel>();

			for (int i = 0; i < changeModelList.size(); ++i)
			{
				ChangeModel cmi = changeModelList.get(i);

				if (processedChangeModels.contains(cmi))
				{
					continue;
				}

				// find all almost equal change models with respect to cmi
				List<ChangeModel> almostEqualChangeModels = new ArrayList<ChangeModel>();
				List<Integer> almostEqualChangeModelIndices = new ArrayList<Integer>();
				for (int j = i; j < changeModelList.size(); ++j)
				{
					ChangeModel cmj = changeModelList.get(j);
					if (areAlmostEqual(cmi, cmj))
					{
						almostEqualChangeModels.add(cmj);
						almostEqualChangeModelIndices.add(changeModelList.indexOf(cmj));
					}
				}

				Collections.sort(almostEqualChangeModels, new AlmostEqualChangeModelComparator());

				// mark all but first change as overridden
				for (int j = 1; j < almostEqualChangeModels.size(); ++j)
				{
					Element element = (Element) changeModelNodeMap.get(almostEqualChangeModels.get(j));
					element.setAttribute("overridden", String.valueOf(true));
				}

				// generate index mapping
				for (int j = 0; j < almostEqualChangeModels.size(); ++j)
				{
					ChangeModel cm = almostEqualChangeModels.get(j);
					orderMap.put(almostEqualChangeModelIndices.get(j), changeModelList.indexOf(cm));
					processedChangeModels.add(cm);
				}
			}

			for (Entry<Integer, Integer> entry : orderMap.entrySet())
			{
				Node node = changeModelNodeMap.get(changeModelList.get(entry.getValue()));
				indexParentMap.get(entry.getKey()).appendChild(node);
			}

			context = JAXBContext.newInstance(InstallationDocument.class);
			unmarshaller = context.createUnmarshaller();

			return unmarshaller.unmarshal(root, InstallationDocument.class).getValue();
		}
		catch (JAXBException e)
		{
			throw new RemindModelException(e);
		}
	}

	private boolean areAlmostEqual(ChangeModel cm1, ChangeModel cm2)
	{
		Target<?, ?> target1 = cm1.getTarget(userInput.getEnvironment());
		Target<?, ?> target2 = cm2.getTarget(userInput.getEnvironment());

		if (target1 == null || target2 == null)
		{
			return false;
		}

		return target1.equals(target2) && cm1.getSource().isAlmostEqual(cm2.getSource());
	}

	private class AlmostEqualChangeModelComparator implements Comparator<ChangeModel>
	{
		@Override
		public int compare(ChangeModel cm1, ChangeModel cm2)
		{
			if (!areAlmostEqual(cm1, cm2))
			{
				return 0;
			}

			// let sources decide about order
			return cm1.getSource().compareTo(cm2.getSource());
		}
	}
}
