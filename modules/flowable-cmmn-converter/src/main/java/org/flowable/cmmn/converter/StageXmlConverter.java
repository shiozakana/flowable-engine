/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.flowable.cmmn.converter;

import javax.xml.stream.XMLStreamReader;

import org.flowable.cmmn.model.CmmnElement;
import org.flowable.cmmn.model.Stage;

/**
 * @author Joram Barrez
 */
public class StageXmlConverter extends PlanItemDefinitiomXmlConverter {
    
    @Override
    public String getXMLElementName() {
        return CmmnXmlConstants.ELEMENT_STAGE;
    }
    
    @Override
    public boolean isCmmnElement() {
        return true;
    }

    @Override
    protected CmmnElement convert(XMLStreamReader xtr, ConversionHelper conversionHelper) {
        Stage stage = new Stage();
        stage.setName(xtr.getAttributeValue(null, CmmnXmlConstants.ATTRIBUTE_NAME));
        stage.setCase(conversionHelper.getCurrentCase());
        stage.setParent(conversionHelper.getCurrentStage());
        
        conversionHelper.setCurrentStage(stage);
        conversionHelper.addStage(stage);
        
        return stage;
    }
    
    @Override
    protected void elementEnd(XMLStreamReader xtr, ConversionHelper conversionHelper) {
        super.elementEnd(xtr, conversionHelper);
        conversionHelper.removeCurrentStage();
    }
    
}