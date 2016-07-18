package org.akaza.openclinica.domain.rule.action;

import org.akaza.openclinica.domain.AbstractMutableDomainObject;
import org.akaza.openclinica.domain.rule.expression.Context;
import org.akaza.openclinica.domain.rule.expression.ExpressionBean;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import javax.persistence.*;

@Entity
@Table(name = "rule_action_stratification_factor")
@GenericGenerator(name = "id-generator", strategy = "native", parameters = { @Parameter(name = "sequence_name", value = "rule_action_stratification_factor_id_seq") })
public class StratificationFactorBean extends AbstractMutableDomainObject {
    
    private ExpressionBean stratificationFactor;
    private RuleActionBean ruleActionBean;
    private String expressionAsString;
    

    
   

    @Transient
    public String getExpressionAsString() {       
        if (getStratificationFactor()!=null && expressionAsString !=null)
            expressionAsString=getStratificationFactor().getValue();
        return expressionAsString;
    }

    public void setExpressionAsString(String expressionAsString) {
      stratificationFactor = new ExpressionBean(Context.OC_RULES_V1, expressionAsString);  
        this.expressionAsString = expressionAsString;
    }
    
    
    

    
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "rule_expression_id")
    public ExpressionBean getStratificationFactor() {
        return stratificationFactor;
    }

    public void setStratificationFactor(ExpressionBean stratificationFactor) {
        this.stratificationFactor = stratificationFactor;
    }

    
    

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "rule_action_id")
    public RuleActionBean getRuleActionBean() {
		return ruleActionBean;
	}



    public void setRuleActionBean(RuleActionBean ruleActionBean) {
		this.ruleActionBean = ruleActionBean;
	}

	@Override
    public String toString() {
        return "StratificationFactorBean [Stratification Factor=" + stratificationFactor + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((stratificationFactor == null) ? 0 : stratificationFactor.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        StratificationFactorBean other = (StratificationFactorBean) obj;
        if (stratificationFactor == null) {
            if (other.stratificationFactor != null)
                return false;
        } else if (!stratificationFactor.equals(other.stratificationFactor))
            return false;
        if (stratificationFactor == null) {
            if (other.stratificationFactor != null)
                return false;
        } else if (!stratificationFactor.equals(other.stratificationFactor))
            return false;
        return true;
    }



}
