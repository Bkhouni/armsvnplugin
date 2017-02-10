/*Copyright (c) "Kinto Soft Ltd"

Subversion ALM is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.*/

package com.kintosoft.jira.jql;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.jira.jql.query.QueryCreationContext;
import com.atlassian.jira.plugin.jql.function.AbstractJqlFunction;
import com.atlassian.jira.plugin.jql.function.JqlFunctionModuleDescriptor;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.jira.util.MessageSetImpl;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.operand.FunctionOperand;
import com.kintosoft.jira.plugin.ext.subversion.ALMMultipleSubversionRepositoryManager;
import com.kintosoft.jira.plugin.ext.subversion.ALMMultipleSubversionRepositoryManagerImpl;
import com.kintosoft.svnwebclient.db.DatabaseFunctions;
import com.kintosoft.svnwebclient.graph.entities.ao.Key;
import com.kintosoft.svnwebclient.jira.SWCUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public abstract class JQLSVNDB extends AbstractJqlFunction {

	protected final static Logger log = LoggerFactory.getLogger(JQLSVNDB.class);

	private final static String DATE_FORMAT = "yyyy-MM-dd";

	private static final List<String> actions;

	private final ActiveObjects ao;

	private final ALMMultipleSubversionRepositoryManager almsrm;


	public JQLSVNDB(ActiveObjects ao, ALMMultipleSubversionRepositoryManager almsrm){

		this.ao = ao;
		this.almsrm = almsrm;
	}

	static {

		actions = new ArrayList<String>();
		actions.add("A");
		actions.add("M");
		actions.add("D");
		actions.add("R");
	}



	protected volatile JqlFunctionModuleDescriptor descriptor;

	abstract protected String usage();

	public void init(final JqlFunctionModuleDescriptor descriptor) {
		this.descriptor = descriptor;
	}

	/*protected Connection getConnection() throws SQLException {
		return PluginConnectionPool.getConnection();

	}*/

	protected List<Key> overrideSecurity(String username, List<Key> keys, boolean overrideSecurity) {
		if (overrideSecurity) {
			for(Key key:keys){
				if(DatabaseFunctions.isIssue(key.getProject(),key.getIssue()))
					keys.remove(key);
			}
			/*sql += " IS_ISSUE(PROJECT, ISSUE)";*/
		} else {
			for(Key key:keys){
				if(DatabaseFunctions.hasVersionControlPermisions(username, key.getProject(), key.getIssue()))
					keys.remove(key);
			}
			/*sql += " HAS_PRIVILEGES_BROWSE(?, PROJECT, ISSUE)";*/
		}
		return keys;
	}

	//VALIANTYS
	protected void loadIssueKeys(List<String> issueKeys, List<Key> rs)
	{
		for(Key key: rs){
			String k = key.getProject() + "-" + key.getIssue();
			if (!issueKeys.contains(k)) {
				issueKeys.add(k);
			}
		}
/*
		while (rs.next()) {
			String key = rs.getString("PROJECT") + "-" + rs.getString("ISSUE");
			if (!issueKeys.contains(key)) {
				issueKeys.add(key);
			}
		}*/
	}

	protected MessageSet validateMimalParameters(FunctionOperand operand) {
		MessageSet messageSet = new MessageSetImpl();
		List<String> args = operand.getArgs();
		if (args.size() < getMinimumNumberOfExpectedArguments()) {
			messageSet
					.addErrorMessage(nicerErrors(operand.getArgs().size() + 1));
		}
		return messageSet;
	}

	@Override
	public MessageSet validate(ApplicationUser searcher,
			FunctionOperand operand, TerminalClause terminalClause) {
		return validateMimalParameters(operand);
	}

	protected MessageSet validate(QueryCreationContext queryCreationContext,
			FunctionOperand operand, TerminalClause terminalClause) {

		String licError = ALMMultipleSubversionRepositoryManagerImpl
				.validateLicense();
		if (!SWCUtils.validateUserPrivilegesForSWC()) {
			MessageSet messageSet = new MessageSetImpl();
			messageSet
					.addWarningMessage("You have not the required permission.\nDepending on your JIRA version its name might be: 'View Version Control', 'View Issue Source Tab' or 'View Development Tools'");
			return messageSet;
		}
		if (licError == null) {
			return validate(queryCreationContext.getUser(), operand,
					terminalClause);
		} else {
			MessageSet messageSet = new MessageSetImpl();
			messageSet.addWarningMessage(licError);
			return messageSet;
		}
	}

	protected String nicerErrors(int param) {
		return getFunctionName() + ":\nError in param #" + param
				+ ".\n\nUSAGE:\n" + usage();
	}

	protected String nicerLine(int index, String name, boolean required,
			String constrains) {
		return "\n#" + index + " " + name.toUpperCase() + " ["
				+ (required ? "Required" : "Optional") + "] " + constrains
				+ ".";
	}

	protected MessageSet validateIntegerParameterEqualsOrGreaterThan(
			MessageSet messageSet, FunctionOperand operand, int index, int min,
			boolean optional) {

		if (optional && operand.getArgs().size() < (index + 1)) {
			return messageSet;
		}
		final String errorMsg = nicerErrors(index + 1);
		try {
			String value = operand.getArgs().get(index);
			if (optional && value.isEmpty()) {
				return messageSet;
			}
			int i = Integer.parseInt(value);
			if (i < min) {
				messageSet.addErrorMessage(errorMsg);
			}
		} catch (NumberFormatException ex) {
			messageSet.addErrorMessage(errorMsg);
		}

		return messageSet;
	}

	protected MessageSet validateStringParameterNotEmpty(MessageSet messageSet,
			FunctionOperand operand, int index) {

		String value = operand.getArgs().get(index);
		if (value.trim().isEmpty()) {
			messageSet.addErrorMessage(nicerErrors(index + 1));
		}
		return messageSet;
	}

	protected MessageSet validateDateOptional(MessageSet messageSet,
			FunctionOperand operand, int index) {

		if (operand.getArgs().size() > index) {
			String value = operand.getArgs().get(index);
			if (value.isEmpty()) {
				return messageSet;
			}
			try {
				getSimpleDateFormat().parse(value);
			} catch (ParseException e) {
				messageSet.addErrorMessage(nicerErrors(index + 1));
			}
		}

		return messageSet;
	}

	public static SimpleDateFormat getSimpleDateFormat() {
		return new SimpleDateFormat(DATE_FORMAT);
	}

	protected String getOptionalString(FunctionOperand operand, int index) {
		String value = "";
		if (operand.getArgs().size() > index) {
			value = operand.getArgs().get(index);
		}
		return value;
	}

	protected int getOptionalInteger(FunctionOperand operand, int index) {
		int limit = 0;
		if (operand.getArgs().size() > index) {
			String value = operand.getArgs().get(index);
			if (!value.isEmpty()) {
				try {
					limit = Integer.parseInt(value);
				} catch (NumberFormatException ex) {

				}
			}

		}
		return limit;
	}

	protected Timestamp string2timestamp(String s, boolean endOfDay) {
		if (s.isEmpty()) {
			Date d;
			if (endOfDay) {
				d = new Date(Long.MAX_VALUE);
			} else {
				d = new Date(0);
			}
			s = getSimpleDateFormat().format(d);
		}
		if (endOfDay) {
			s += " 23:59:59.999999999";
		}
		Date d;
		try {
			d = getSimpleDateFormat().parse(s);
			Timestamp ts = new Timestamp(d.getTime());
			return ts;
		} catch (ParseException e) {
			log.error(e.getMessage());
			return new Timestamp(new Date().getTime());
		}
	}

	protected MessageSet validateActionOptional(MessageSet messageSet,
			FunctionOperand operand, int index) {

		if (operand.getArgs().size() <= index) {
			return messageSet;
		}
		String value = operand.getArgs().get(index);

		if (value.isEmpty()) {
			return messageSet;
		}

		if (!actions.contains(value)) {
			messageSet.addErrorMessage(nicerErrors(index + 1));
		}

		return messageSet;
	}
}
