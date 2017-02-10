package org.polarion.svnwebclient.web.model.data.directory;

import org.polarion.svnwebclient.data.model.DataDirectoryElement;
import org.polarion.svnwebclient.data.model.DataRepositoryElement;
import org.polarion.svnwebclient.decorations.IIconDecoration;
import org.polarion.svnwebclient.decorations.IRevisionDecorator;
import org.polarion.svnwebclient.util.*;
import org.polarion.svnwebclient.web.resource.Images;
import org.polarion.svnwebclient.web.resource.Links;
import org.polarion.svnwebclient.web.support.AbstractRequestHandler;
import org.polarion.svnwebclient.web.support.RequestParameters;
import org.polarion.svnwebclient.web.support.State;

import java.util.Comparator;

public class BaseDirectoryContent {
	protected DataDirectoryElement directoryElement;
	protected AbstractRequestHandler requestHandler;
	protected long headRevision;
	protected String url;
	protected IRevisionDecorator revisionDecorator;
	protected State state;
	protected Comparator comparator;

	public class BaseElement {
		protected DataRepositoryElement repositoryElement;

		public BaseElement(DataRepositoryElement repositoryElement) {
			this.repositoryElement = repositoryElement;
		}

		public String getImage() {
			if (this.repositoryElement.isDirectory()) {
				return Images.DIRECTORY;
			} else {
				return Images.FILE;
			}
		}

		public String getName() {
			return HtmlUtil.encode(this.repositoryElement.getName(), false);
		}

		public String getDecoratedRevision() {
			String ret = null;
			ret = NumberFormatter.format(this.repositoryElement.getRevision());
			return ret;
		}

		public String getRevision() {
			return HtmlUtil.encode(Long.toString(this.repositoryElement
					.getRevision()));
		}

		public boolean isHeadRevision() {
			return BaseDirectoryContent.this.headRevision == this.repositoryElement
					.getRevision();
		}

		public boolean isRevisionDecorated() {
			return BaseDirectoryContent.this.revisionDecorator
					.isRevisionDecorated(this.getRevision(),
							BaseDirectoryContent.this.state.getRequest());
		}

		public IIconDecoration getRevisionDecoration() {
			return BaseDirectoryContent.this.revisionDecorator
					.getIconDecoration(this.getRevision(),
							BaseDirectoryContent.this.state.getRequest());
		}

		public String getDate() {
			return DateFormatter.format(this.repositoryElement.getDate());
		}

		public String getAge() {
			return DateFormatter.format(this.repositoryElement.getDate(),
					DateFormatter.RELATIVE);
		}

		public String getAuthor() {
			String res = this.repositoryElement.getAuthor();
			return res == null ? "" : HtmlUtil.encode(res);
		}

		public String getFirstLine() {
			return CommentUtil
					.getFirstLine(this.repositoryElement.getComment());
		}

		public boolean isMultiLineComment() {
			return CommentUtil.isMultiLine(this.repositoryElement.getComment());
		}

		public String getComment() {
			return HtmlUtil.encode(this.repositoryElement.getComment());
		}

		public String getTooltip() {
			return CommentUtil.getTooltip(this.repositoryElement.getComment());
		}

		public String getSize() {
			String ret;
			if (this.repositoryElement.isDirectory()) {
				ret = "<DIR>";
			} else {
				ret = NumberFormatter.format(this.repositoryElement.getSize());
			}
			return HtmlUtil.encode(ret);
		}

		public boolean isRestricted() {
			return this.repositoryElement.isRestricted();
		}
	}

	public BaseDirectoryContent(DataDirectoryElement directoryElement,
			AbstractRequestHandler requestHandler,
			IRevisionDecorator revisionDecorator) {
		this.directoryElement = directoryElement;
		this.requestHandler = requestHandler;
		this.revisionDecorator = revisionDecorator;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public void setState(State state) {
		this.state = state;
	}

	public void setHeadRevision(long headRevision) {
		this.headRevision = headRevision;
	}

	public void applySort(Comparator comparator) {
		this.comparator = comparator;
	}

	public String getAuthor() {
		return HtmlUtil.encode(this.directoryElement.getAuthor());
	}

	public String getComment() {
		return HtmlUtil.encode(this.directoryElement.getComment());
	}

	public String getTooltip() {
		return CommentUtil.getTooltip(this.directoryElement.getComment());
	}

	public String getFirstLine() {
		return CommentUtil.getFirstLine(this.directoryElement.getComment());
	}

	public boolean isMultiLineComment() {
		return CommentUtil.isMultiLine(this.directoryElement.getComment());
	}

	public String getDate() {
		return DateFormatter.format(this.directoryElement.getDate());
	}

	public String getAge() {
		return DateFormatter.format(this.directoryElement.getDate(),
				DateFormatter.RELATIVE);
	}

	public String getRevision() {
		return Long.toString(this.directoryElement.getRevision());
	}

	public String getDecoratedRevision() {
		return NumberFormatter.format(this.directoryElement.getRevision());
	}

	public String getRevisionUrl() {
		UrlGenerator urlGenerator = UrlGeneratorFactory.getUrlGenerator(
				Links.REVISION, this.requestHandler.getLocation());
		String url = this.url;
		urlGenerator.addParameter(RequestParameters.URL, UrlUtil.encode(url));
		if (this.requestHandler.getCurrentRevision() != -1) {
			urlGenerator.addParameter(RequestParameters.CREV,
					Long.toString(this.requestHandler.getCurrentRevision()));
		}
		urlGenerator.addParameter(RequestParameters.REV, this.getRevision());
		return urlGenerator.getUrl();
	}

	public boolean isHeadRevision() {
		return this.headRevision == this.directoryElement.getRevision();
	}

	public String getChildCount() {
		return Integer
				.toString(this.directoryElement.getChildElements().size());
	}
}
