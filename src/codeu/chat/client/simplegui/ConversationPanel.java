// Copyright 2017 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//    http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package codeu.chat.client.simplegui;

import java.util.HashSet;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import codeu.chat.client.ClientContext;
import codeu.chat.common.ConversationSummary;

// NOTE: JPanel is serializable, but there is no need to serialize ConversationPanel
// without the @SuppressWarnings, the compiler will complain of no override for serialVersionUID
@SuppressWarnings("serial")
public final class ConversationPanel extends JPanel {

  private final ClientContext clientContext;
  private final MessagePanel messagePanel;
  private final DefaultListModel<String> convListModel = new DefaultListModel<>();
  private final JList<String> conversationList = new JList<>(convListModel);

  public ConversationPanel(ClientContext clientContext, MessagePanel messagePanel) {
    super(new GridBagLayout());
    this.clientContext = clientContext;
    this.messagePanel = messagePanel;
    initialize();
  }

  private void initialize() {

    // This panel contains from top to bottom: a title bar,
    // a list of conversations, and a button bar.

    // Title
    final JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
    titlePanel.setBackground(new Color(216, 216, 216));
    final GridBagConstraints titlePanelC = new GridBagConstraints();
    titlePanelC.gridx = 0;
    titlePanelC.gridy = 0;
    titlePanelC.anchor = GridBagConstraints.PAGE_START;

    final JLabel titleLabel = new JLabel("CONVERSATIONS", JLabel.LEFT);
    titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
    titleLabel.setFont(new Font("Lucida Grande", Font.BOLD, 15));
    titleLabel.setForeground(new Color(188, 32, 49));
    titlePanel.add(titleLabel);

    // Conversation list
    final JPanel listShowPanel = new JPanel();
    listShowPanel.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
    listShowPanel.setBackground(new Color(216, 216, 216));
    final GridBagConstraints listPanelC = new GridBagConstraints();

    conversationList.setFixedCellHeight(25); // adjusts spacing in between cells
    conversationList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    conversationList.setVisibleRowCount(15);
    conversationList.setSelectedIndex(-1);
    conversationList.setCellRenderer(new ConversationRenderer());
    conversationList.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5)); // top, left, bottom, right
    conversationList.setOpaque(false);

    final JScrollPane listScrollPane = new JScrollPane(conversationList);
    listShowPanel.add(listScrollPane);
    listScrollPane.setOpaque(false);
    listScrollPane.setBackground(new Color(238, 238, 238));
    listScrollPane.setMinimumSize(new Dimension(260, 200));
    listScrollPane.setPreferredSize(new Dimension(260, 320));
    listScrollPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    
    // scrollbar visual components
    listScrollPane.getVerticalScrollBar().setUI(new BasicScrollBarUI()
    {
      protected JButton createDecreaseButton(int orientation) {
        JButton button = super.createDecreaseButton(orientation);
        modifyButton(button);
        return button;
      }
      
      protected JButton createIncreaseButton(int orientation) {
        JButton button = super.createIncreaseButton(orientation);
        modifyButton(button);
        return button;
      }
      
      // helper method to change increment & decrement buttons to a default style
      private JButton modifyButton(JButton button) {
        button.setBackground(new Color(188, 32, 49, 0));
        button.setForeground(new Color(188, 32, 49, 0));
        button.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        return button;
      }

      protected void configureScrollBarColors() {
        trackColor = new Color(255, 255, 255, 0);
        thumbColor = new Color(188, 32, 49);
      }
    });

    // Button bar
    final JPanel buttonPanel = new JPanel();
    buttonPanel.setBackground(new Color(216, 216, 216));
    final GridBagConstraints buttonPanelC = new GridBagConstraints();

    final JButton updateButton = new JButton("update");
    updateButton.setBackground(new Color(188, 32, 49));
    updateButton.setFont(new Font("Lucida Grande", Font.PLAIN, 13));
    updateButton.setForeground(Color.WHITE);
    updateButton.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

    final JButton addButton = new JButton("add");
    addButton.setBackground(new Color(188, 32, 49));
    addButton.setForeground(Color.WHITE);
    addButton.setFont(new Font("Lucida Grande", Font.PLAIN, 13));
    addButton.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));

    updateButton.setAlignmentX(Component.LEFT_ALIGNMENT);
    buttonPanel.add(updateButton);
    buttonPanel.add(addButton);

    // Put panels together
    titlePanelC.gridx = 0;
    titlePanelC.gridy = 0;
    titlePanelC.gridwidth = 10;
    titlePanelC.gridheight = 4;
    titlePanelC.fill = GridBagConstraints.HORIZONTAL;
    titlePanelC.anchor = GridBagConstraints.FIRST_LINE_START;

    listPanelC.gridx = 0;
    listPanelC.gridy = 4;
    listPanelC.gridwidth = 10;
    listPanelC.gridheight = 4;
    listPanelC.fill = GridBagConstraints.BOTH;
    listPanelC.anchor = GridBagConstraints.FIRST_LINE_START;
    listPanelC.weightx = 0.8;
    listPanelC.weighty = 0.5;

    buttonPanelC.gridx = 0;
    buttonPanelC.gridy = 8;
    buttonPanelC.gridwidth = 10;
    buttonPanelC.gridheight = 4;
    buttonPanelC.fill = GridBagConstraints.HORIZONTAL;
    buttonPanelC.anchor = GridBagConstraints.FIRST_LINE_START;

    this.add(titlePanel, titlePanelC);
    this.add(listShowPanel, listPanelC);
    this.add(buttonPanel, buttonPanelC);

    // User clicks Conversations Update button.
    updateButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        final String selected = conversationList.getSelectedValue();
        ConversationPanel.this.getAllConversations();
        conversationList.setSelectedValue(selected, false);
      }
    });

    // User clicks Conversations Add button.
    addButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        ConversationPanel.this.getNewConversations();
        if (clientContext.user.hasCurrent()) {
          final String s = (String) JOptionPane.showInputDialog(
              ConversationPanel.this, "Enter title:", "Add Conversation", JOptionPane.PLAIN_MESSAGE,
              null, null, "");
          if (s != null && s.length() > 0) {
            ConversationPanel.this.getAllConversations();
            // No duplicate names are allowed
            if (ConversationPanel.this.convListModel.contains(s)) {
              JOptionPane.showMessageDialog(ConversationPanel.this, "Conversation already exists");
            } else if (s.length() > 35) { // conversation name length should not exceed 35 characters
              JOptionPane.showMessageDialog(ConversationPanel.this, "Max length for a conversation name is 35 characters");
            } else {
              clientContext.conversation.startConversation(s, clientContext.user.getCurrent().id);
              ConversationPanel.this.getNewConversations();
            }
            conversationList.setSelectedValue(s, true);
          }
        } else {
          JOptionPane.showMessageDialog(ConversationPanel.this, "You are not signed in.");
        }
      }
    });

    // User clicks on Conversation - Set Conversation to current and fill in Messages panel.
    conversationList.addListSelectionListener(new ListSelectionListener() {
      @Override
      public void valueChanged(ListSelectionEvent e) {
        if (conversationList.getSelectedIndex() != -1) {
          final int index = conversationList.getSelectedIndex();
          final String data = conversationList.getSelectedValue();
          final ConversationSummary cs = ConversationPanel.this.lookupByTitle(data, index);

          clientContext.conversation.setCurrent(cs);

          messagePanel.update(clientContext.conversation.getCurrent());
        }
      }
    });

    getAllConversations();
  }

  /**
   * Populate ListModel - updates display objects.
   */
  public void getNewConversations() {

    // Get all of the conversations
    clientContext.conversation.updateAllConversations(false);

    // Store all of the titles that are received in a HashSet
    HashSet<String> titles = new HashSet<>();
    for (final ConversationSummary conv : clientContext.conversation.getConversationSummaries()) {
      // If the title has not been displayed, display it
      if (!convListModel.contains(conv.title)){
        convListModel.addElement(conv.title);
      }

      // Remember that this title has been displayed
      titles.add(conv.title);
    }

    // Remove any titles that no longer exist
    for (int i = 0; i < convListModel.size(); i++) {
      if (!titles.contains(convListModel.getElementAt(i))){
        convListModel.removeElementAt(i);
      }
    }
  }

  /**
   * Force the conversations list to reload all of the titles
   */
  private void getAllConversations() {
    convListModel.clear();
    getNewConversations();
  }

  /**
   * Locate the Conversation object for a selected title string.
   * index handles possible duplicate titles.
   */
  private ConversationSummary lookupByTitle(String title, int index) {
    int localIndex = 0;
    for (final ConversationSummary cs : clientContext.conversation.getConversationSummaries()) {
      if ((localIndex >= index) && cs.title.equals(title)) {
        return cs;
      }
      localIndex++;
    }
    return null;
  }
}
