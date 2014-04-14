/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.core.commons.services.video;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;

import org.jcodec.common.FileChannelWrapper;
import org.jcodec.containers.mp4.demuxer.MP4Demuxer;
import org.olat.core.commons.services.image.Size;
import org.olat.core.commons.services.video.spi.FLVParser;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.vfs.LocalFileImpl;
import org.olat.core.util.vfs.VFSLeaf;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 04.04.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class MovieServiceImpl implements MovieService {
	
	private static final OLog log = Tracing.createLoggerFor(MovieServiceImpl.class);

	@Override
	public Size getSize(VFSLeaf media, String suffix) {
		File file = ((LocalFileImpl)media).getBasefile();
		if(suffix.equals("mp4") || suffix.equals("m4v")) {
			try(RandomAccessFile accessFile = new RandomAccessFile(file, "r")) {
				FileChannel ch = accessFile.getChannel();
				FileChannelWrapper in = new FileChannelWrapper(ch);
				MP4Demuxer demuxer1 = new MP4Demuxer(in);
				org.jcodec.common.model.Size size = demuxer1.getMovie().getDisplaySize();
				int w = size.getWidth();
				int h = size.getHeight();
				return new Size(w, h, false);
			} catch (IOException e) {
				log.error("Cannot extract size of: " + media, e);
			}
		} else if(suffix.equals("flv")) {
			try(InputStream stream = new FileInputStream(file)) {
				FLVParser infos = new FLVParser();
				infos.parse(stream);
				if(infos.getWidth() > 0 && infos.getHeight() > 0) {
					int w = infos.getWidth();
					int h = infos.getHeight();
					return new Size(w, h, false);
				}
			} catch (Exception e) {
				log.error("Cannot extract size of: " + media, e);
			}
		}
		return null;
	}
	
	

}