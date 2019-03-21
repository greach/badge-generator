@Grapes(
        @Grab(group='org.apache.pdfbox', module='pdfbox', version='2.0.8')
)
import java.io.File; 
import java.io.IOException;
  
import org.apache.pdfbox.pdmodel.PDDocument; 
import org.apache.pdfbox.pdmodel.PDPage; 
import org.apache.pdfbox.pdmodel.PDPageContentStream; 
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.PDType0Font;

final String DELIMITER = ";"

import groovy.transform.EqualsAndHashCode

@EqualsAndHashCode
class Attendee {
    String name
    String twitter
    String company
}

List<Attendee> attendees = []
def actualCount = 0

new File('attendees.csv').eachLine { line ->
    String[] arr = line.split(DELIMITER)
    Attendee attendee = new Attendee()
    if (arr.size() >= 1) {
        attendee.name = arr[0]
    }
    if (arr.size() >= 2) {
        attendee.twitter = arr[1]
    }
    if (arr.size() >= 3) {
        attendee.company = arr[2]
    }
    attendees << attendee
}
attendees = attendees.unique()


final int PAGE_SIZE = 8
final int COMPANY_Y_OFFSET = 50
final int TWITTER_Y_OFFSET = 25
final int X_OFFSET = 150
final int Y_OFFSET = 250
final int NAME_FONT_SIZE = 22
final int TWITTER_FONT_SIZE = 18
final int COMPANY_FONT_SIZE = 18

int FIRST_COLUMN = 15
int SECOND_COLUMN = 275
int BOTTOM_OFFSET = 20
int BADGESIZE = 180
List<List<Integer>> positions = [
  [FIRST_COLUMN, BOTTOM_OFFSET + BADGESIZE],
  [SECOND_COLUMN,BOTTOM_OFFSET + BADGESIZE],
  [FIRST_COLUMN, BOTTOM_OFFSET + (BADGESIZE * 2)],
  [SECOND_COLUMN, BOTTOM_OFFSET + (BADGESIZE * 2)],
  [FIRST_COLUMN, BOTTOM_OFFSET + (BADGESIZE * 3)],
  [SECOND_COLUMN, BOTTOM_OFFSET + (BADGESIZE * 3)],
  [FIRST_COLUMN, BOTTOM_OFFSET + (BADGESIZE * 4)],
  [SECOND_COLUMN, BOTTOM_OFFSET + (BADGESIZE * 4)],
]

int count = 1
for (i = 0; i < attendees.size(); i += PAGE_SIZE) {
    List<Attendee> attendeeBatch = attendees.subList(i, Math.min(attendees.size(), i+PAGE_SIZE))
    File badgeTemplate = new File('BadgeTemplate.pdf')
    PDDocument document = PDDocument.load(badgeTemplate);
    PDType0Font font = PDType0Font.load(document, new File('LiberationSans-Regular.ttf'))
    PDPage page = document.getPage(0);
    PDPageContentStream contentStream = new PDPageContentStream(document, page,  PDPageContentStream.AppendMode.APPEND, true, true);
    for (int attendeeCount = 0; attendeeCount < attendeeBatch.size(); attendeeCount++) {
        Attendee attendee = attendeeBatch[attendeeCount]
        int nameX = positions[attendeeCount][0]
        int nameY = positions[attendeeCount][1]
        if (attendee.name) {
              
            contentStream.with {
                beginText()
                setFont(font, NAME_FONT_SIZE);
                newLineAtOffset(nameX, nameY)
                String name = ""
                int maxLength = 18
                for (String word : attendee.name.split(' ')) {
                   name += word + " " 
                   if (name.length() >= maxLength) {
			break
	           }		
                }
                showText(name);     
                endText()
            }
        }
        if (attendee.twitter) {
            int twitterX = nameX
            int twitterY = nameY - TWITTER_Y_OFFSET 
            contentStream.with {
                beginText()
                setFont(font, TWITTER_FONT_SIZE);
                newLineAtOffset(twitterX, twitterY)
                showText(attendee.twitter.startsWith('@') ? attendee.twitter : "@${attendee.twitter}");     
                endText()
            } 
        } 
        if (attendee.company) {
            int companyX = nameX 
            int companyY = nameY - COMPANY_Y_OFFSET
            contentStream.with {
                beginText()
                setFont(font, COMPANY_FONT_SIZE);
                newLineAtOffset(companyX, companyY)
                showText(attendee.company);     
                endText()
            } 
        }
    }
    contentStream.close();
    document.save(new File("Badge-${count++}.pdf"))
    document.close()
}




      

