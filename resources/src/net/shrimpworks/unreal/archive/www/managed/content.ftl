<#include "../_header.ftl">

<#assign headerbg>${static!"static"}/images/none-managed.png</#assign>

<#if content.managed.titleImage??>
	<#assign headerbg>${content.managed.titleImage}</#assign>
</#if>

	<section class="header" style="background-image: url('${headerbg}')">
		<h1>
			${title}
		</h1>
	</section>

	<article class="managed document">
		<section class="meta">
			<h2>Information</h2>
			<div class="label-value">
				<label>Author</label><span>${content.managed.author}</span>
			</div>
			<#if content.managed.homepage??>
				<div class="label-value">
					<label> </label><span><a href="${content.managed.homepage}">Homepage</a></span>
				</div>
			</#if>
			<div class="label-value">
				<label>Release Date</label><span>${content.managed.releaseDate!"-"}</span>
			</div>
			<div class="label-value">
				<label>Date Added</label><span>${content.managed.createdDate}</span>
			</div>
			<div class="label-value">
				<label>Last Updated</label><span>${content.managed.updatedDate}</span>
			</div>
			<div class="label-value">
				<label>Summary</label><span>${content.managed.description}</span>
			</div>

			<section class="downloads">
				<h2>Downloads</h2>
				<#list content.managed.downloads as d>
					<#if !d.deleted && d.downloads?size gt 0>
						<div class="download">
							<div class="title">
								<img src="${static!"static"}/images/icons/black/px22/os-${d.platform?lower_case}.png" title="${d.platform}" align="absmiddle" alt="${d.platform}"/>
								${d.title} ver ${d.version}
							</div>
							<div class="info">
								<div>${fileName(d.localFile)}</div>
								<div>${fileSize(d.fileSize)}</div>
								<div>${d.description}</div>
							</div>
							<div class="links">
								<#list d.downloads as l>
									<a href="${urlEncode(l)}">
										<img src="${static!"static"}/images/icons/black/px22/download.png" align="absmiddle" alt="Download"/>
										${urlHost(l)}
									</a>
								</#list>
							</div>
						</div>
					</#if>
				</#list>
			</section>
		</section>

		<section class="content readable">
			${document}
		</section>
	</article>

<#include "../_footer.ftl">