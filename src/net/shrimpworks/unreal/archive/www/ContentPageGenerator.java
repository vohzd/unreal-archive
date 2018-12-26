package net.shrimpworks.unreal.archive.www;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.http.client.HttpResponseException;
import org.apache.http.client.fluent.Request;

import net.shrimpworks.unreal.archive.Util;
import net.shrimpworks.unreal.archive.indexer.Content;
import net.shrimpworks.unreal.archive.indexer.ContentManager;

public abstract class ContentPageGenerator {

	final ContentManager content;
	final Path root;
	final Path staticRoot;

	private final boolean localImages;

	/**
	 * Create a new Page Generator instance.
	 *
	 * @param content     content manager
	 * @param output      path to write this generator's output to
	 * @param staticRoot  path to static content
	 * @param localImages if true, download and reference local copies of remote images
	 */
	public ContentPageGenerator(ContentManager content, Path output, Path staticRoot, boolean localImages) {
		this.content = content;
		this.root = output;
		this.staticRoot = staticRoot;
		this.localImages = localImages;
	}

	/**
	 * Generate one or more HTML pages of output.
	 *
	 * @return number of individual pages created
	 */
	public abstract int generate();

	/**
	 * Download and store image files locally.
	 * <p>
	 * This works by replacing in-memory image attachment URLs
	 * with local paths for the purposes of outputting HTML pages
	 * linking to local copies, rather than the original remotes.
	 *
	 * @param content   content item to download images for
	 * @param localPath local output path
	 * @throws IOException something probably broke
	 */
	void localImages(Content content, Path localPath) throws IOException {
		if (!localImages) return;

		// find all the images
		List<Content.Attachment> images = content.attachments.stream()
															 .filter(a -> a.type == Content.AttachmentType.IMAGE)
															 .collect(Collectors.toList());

		// we're creating a sub-directory here, to create a nicer looking on-disk structure
		Path imgPath = localPath.resolve("images");
		if (!Files.exists(imgPath)) Files.createDirectories(imgPath);

		for (Content.Attachment img : images) {
			try {
				System.out.printf("\rDownloading image %-60s", img.name);

				// prepend filenames with the content hash, to prevent conflicts
				String hashName = String.join("_", content.hash.substring(0, 8), img.name);
				Path outPath = imgPath.resolve(hashName);

				// only download if it doesn't already exist locally
				if (!Files.exists(outPath)) Request.Get(Util.toUriString(img.url)).execute().saveContent(outPath.toFile());

				// replace the actual attachment with the local copy
				content.attachments.remove(img);
				content.attachments.add(new Content.Attachment(img.type, img.name, localPath.relativize(outPath).toString()));
			} catch (HttpResponseException e) {
				System.err.printf("\rFailed to download image %s: %d %s%n", img.name, e.getStatusCode(), e.getMessage());
			} catch (Throwable t) {
				System.err.printf("\rFailed to download image %s: %s%n", img.name, t.toString());
			}
		}
	}
}
